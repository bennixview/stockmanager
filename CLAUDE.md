# stockmanager

## Commit-Konventionen

- Commit-Messages enthalten **keine** Co-Authored-By-, Claude-Session- oder
  sonstigen Tool-/Modell-Trailer. `includeCoAuthoredBy` ist in
  `.claude/settings.json` auf `false` gesetzt; auch manuell dürfen solche
  Zeilen nicht ergänzt werden.
- Author und Committer sind der Repository-Eigentümer:
  `Benjamin Wirtz <2143827+bennixview@users.noreply.github.com>`.
  Vor dem ersten Commit einer Session setzen:

  ```sh
  git config user.name "Benjamin Wirtz"
  git config user.email "2143827+bennixview@users.noreply.github.com"
  ```
- Kein Modell- oder Tool-Name in Commit-Messages, PR-Titeln, PR-Bodies oder
  Code-Kommentaren.
