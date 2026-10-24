# Instructions Copilot — sf3jswing

## Contexte

sf3jswing est un framework et moteur de jeu de combat 2D 1v1 en Java (licence LGPL-3.0-or-later, statut alpha, environ 90-95 % terminé). Mainteneur : b23prodtm. Les sources Bitbucket n'existent plus : ce dépôt est reconstruit à partir des archives SourceForge (2009-2015) et, si besoin, des artefacts Maven Central `net.sf.sf3jswing:*`. L'historique git d'origine est perdu (voir `docs/PROVENANCE.md`). Objectif : finaliser le projet sans en changer la structure.

Modules connus d'après les builds SourceForge (`docs/AUDIT.md` fait foi une fois produit) :
`sf3jswing-jxakernel` (noyau), `sf3jswing-jigaxtended` (projets Ant), `sf3jswing-jxatest-unit`, `sf3jswing-imagemulticonverter-ant`, `sf3jswing-imcbrowser`, `sf3jswing-animsbrowser-ant`, `sf3jswing-thegame-ant`.

Dépendances connues : LWJGL 2.x (fork `b23prodtm/lwjgl`, tag `v.2.9.3-jxa`), JInput, JAI, artefacts Maven Central `net.sf.sf3jswing:*`.

## Règles non négociables

1. **Java 17** : le code et les builds ciblent Java 17 (`-source 17 -target 17`). Ne pas réintroduire de dépendance ou d'API Java 8-only.
2. **Licence LGPL-3.0-or-later** : conserver les avis de droits d'auteur et notices des composants tiers. Vérifier la compatibilité des nouvelles dépendances avec LGPL-3.0-or-later et indiquer leur licence dans la PR.
3. **Structure d'origine** : ne pas renommer packages, modules ni formats de fichiers (personnages, niveaux, sprites). Les formats restent rétrocompatibles, vérifiés en chargeant les exemples d'origine.
4. **Ant reste le build de référence** tant qu'un build Maven ne produit pas des jars équivalents (comparer leur contenu). Ne supprimer aucun `build.xml`.
5. **Livrables** : jar exécutable (`java -jar`), zip portable avec lanceurs (`.sh`, `.bat`, `.command`), installable avec OpenJDK + Ant/Maven. Aucun outil propriétaire.
6. **Aucun secret dans le dépôt** : jamais de keystore, certificat, mot de passe ni token. La signature des jars est optionnelle et utilise des secrets GitHub. Ne pas committer de nouveaux binaires natifs : les récupérer depuis les releases ou Maven.
7. **Hors périmètre sauf demande explicite** : Gradle, Kotlin, LWJGL 3, migration Swing → JavaFX, renommage de packages, reformatage global (pas de bruit dans les diffs), Java > 8 dans le code.

## Méthode de travail

- Une issue = une PR courte et relisible (cible : moins de 40 fichiers et 400 lignes modifiées hors fichiers générés). Sinon, découper.
- Avant de coder : lire `docs/AUDIT.md` et `docs/COPILOT-PHASES.md`. Ne pas inventer de commandes de build : utiliser celles de l'audit.
- Compiler et tester avant d'ouvrir la PR. Décrire dans la PR : ce qui change, comment le tester, les risques, ce qui n'a pas pu être testé (rendu OpenGL, manette physique, réseau réel).
- **S'arrêter et poser la question dans la PR** si : 3 échecs consécutifs de la CI, changement d'API publique, de format de fichier, de licence ou de dépendance. Ne pas deviner.
- Tests : JUnit 4, exécutables en headless (`-Djava.awt.headless=true`, rendu logiciel Java2D). Aucun accès réseau externe ni GPU. Tests déterministes : graine fixe, pas de `sleep` long.
- Style : suivre les conventions du fichier modifié. Commits en anglais (Conventional Commits). Javadoc et commentaires des nouveaux fichiers en anglais. Tenir `CHANGELOG.md` à jour.
- CI : GitHub Actions, permissions minimales (`contents: read` par défaut), actions épinglées par version majeure.
- Réseau : borner et valider tout paquet reçu (taille, plages de valeurs). Jamais de désérialisation d'objets Java (`ObjectInputStream`) sur des données réseau.
