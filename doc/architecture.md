# Architektur

Zwei Self-Contained Systems, jedes ein eigener Hexagon. Dieses Dokument sagt, warum der Schnitt so
liegt und woran man erkennt, dass er hält.

## Warum zwei Systeme

Depotverwaltung und Marktdaten sind unterschiedliche Domänen mit unterschiedlichem Takt:

* Ein Trade wird selten gebucht, muss aber dauerhaft und korrekt sein. Die Daten gehören dem Nutzer.
* Ein Kurs ist sekundenaktuell, in einer Minute wertlos und gehört einem Datenanbieter.

Sie zusammen in eine Anwendung zu legen hieße, Verfügbarkeit und Release-Zyklus zu koppeln: Ein
Ausfall der Kursquelle dürfte dann das Depot mitreißen. Getrennt gilt: fällt das Marktdaten-System
aus, bleibt das Depot lesbar und buchbar, die Positionen sind nur "ohne Kurs".

### Was die Systeme *nicht* teilen

Keine Datenbank, keine gemeinsame Bibliothek mit Fachlogik, keine gemeinsamen DTOs. `Money`, `Symbol`,
`Isin` und `Wkn` gibt es in beiden Systemen – bewusst als Kopie. Ein gemeinsames Artefakt wäre eine
Kopplung, die beim nächsten Release beider Systeme auffällt. Geteilt wird nur der HTTP-Vertrag, und
den spiegelt jede Seite in ihren eigenen Typen (`MarketDataRestAdapter.QuoteDto`).

## Integration

| Ebene | Wie | Wo im Code |
|---|---|---|
| Daten | HTTP GET auf `/api/quotes` und `/api/instruments` | `MarketDataPort` → `MarketDataRestAdapter` |
| UI | Web Component `<market-quote>`, vom fremden Origin geladen | `market-quote.js` im Marktdaten-System |
| Live | Server-Sent Events, Browser hängt direkt am Marktdaten-System | `QuoteStreamController` |
| Navigation | Links zwischen den Systemen, konfigurierte URLs | `MarketDataUiIntegration`, `UiModelAttributes` |

Devisenkurse sind keine eigene Integration: `EURUSD=X` ist für das Marktdaten-System ein Symbol wie
jedes andere, also holt `MarketDataExchangeRates` sie über denselben Port.

## Der Hexagon

```
                    ┌──────────── infrastructure ────────────┐
   HTTP, Browser ──►│ adapter.in.web    adapter.in.legacy    │
                    │        │                  │            │
                    │        ▼                  ▼            │
                    │  ┌──────── application ────────┐       │
                    │  │  port.in  →  service        │       │
                    │  │                  │          │       │
                    │  │  ┌──────── domain ───────┐  │       │
                    │  │  │ Portfolio, Position,  │  │       │
                    │  │  │ Money, Repository-Port│  │       │
                    │  │  └───────────────────────┘  │       │
                    │  │                  │          │       │
                    │  │              port.out       │       │
                    │  └─────────────────┬───────────┘       │
                    │                    ▼                   │
                    │ adapter.out.persistence  .marketdata   │──► DB, anderes SCS
                    └────────────────────────────────────────┘
                                bootstrap verdrahtet alles
```

Entscheidend: **`domain` und `application` enthalten keine Framework-Annotation.** Kein `@Service`,
kein `@Entity`, kein Jackson. Was ein Use Case braucht, steht in seinem Konstruktor; wer es liefert,
entscheidet `PortfolioConfiguration` bzw. `MarketDataConfiguration` im `bootstrap`-Modul.

Das kostet ein paar Zeilen Verdrahtung und bringt dafür:

* Domänentests laufen in Millisekunden, ohne Spring-Kontext
* der Austausch eines Adapters – JSON-Datei gegen JPA, Alpha Vantage gegen Yahoo – berührt keinen
  Use Case
* die Abhängigkeitsrichtung ist prüfbar statt nur gemeint

## Prüfbar statt gemeint

`HexagonalArchitectureTest` (ArchUnit, je System) prüft bei jedem Build:

* Abhängigkeiten zeigen nach innen (`layeredArchitecture`)
* Domäne und Anwendungsschicht kennen kein Framework
* treibende Adapter hängen an Ports, nicht an deren Implementierungen
* kein System greift in den Code des anderen
* keine Zyklen zwischen Paketen
* Repositories sind Interfaces in der Domäne
* keine Feldinjektion
* die jMolecules-DDD-Regeln über Aggregate, Entities und Value Objects

## Entscheidungen und ihre Kosten

**Aggregat als Ganzes speichern.** `PortfolioPersistenceAdapter` schreibt Positionen und Tranchen
komplett neu, statt Änderungen zu diffen. Einfach und immer konsistent, aber nichts für Depots mit
zehntausenden Tranchen. Bis dahin ist die Aggregatgrenze die Transaktionsgrenze, und `@Version`
schützt vor verlorenen Updates.

**FIFO fest verdrahtet.** Verkäufe bedienen die ältesten Tranchen zuerst – so rechnet das deutsche
Steuerrecht. Andere Verfahren (LIFO, Durchschnitt) wären eine Strategie im Domänenmodell; solange
niemand sie braucht, wäre das Spekulation.

**Kurse werden nur geholt, wenn jemand hinsieht.** Der Refresh-Zyklus fragt ausschließlich Symbole
ab, für die eine Subscription offen ist. Ohne offenen Browser kostet das System die Kursquelle
nichts.

**Ein veralteter Kurs schlägt keinen Kurs.** Fällt die Quelle aus, liefert der `QuoteService` den
letzten bekannten Wert weiter, statt eine Lücke zu zeigen. Das Alter steht im Tooltip des Widgets.

**Positionen ohne Kurs verschwinden nicht.** Sie fallen aus den Summen heraus und werden separat
ausgewiesen – lieber eine ehrliche Lücke als eine stille Falschsumme.

**Kein Lombok, kein MapStruct.** Records und ein paar handgeschriebene Mapper in den Adaptern
reichen; Mapping ist Adapterarbeit und darf sichtbar sein.

## Datenbank

Beide Systeme migrieren mit Flyway; die Migrationen liegen beim Persistenz-Adapter, der das Schema
besitzt. Standard ist eine eingebettete H2-Datei unter `./data`, für den ernsteren Betrieb gibt es
das Profil `postgres`. Hibernate läuft mit `ddl-auto: validate` – das Schema kommt aus den
Migrationen, nie aus dem Mapping.
