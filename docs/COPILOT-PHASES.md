# Plan de finalisation sf3jswing — phases pour Copilot

Chaque section est le corps d'une issue GitHub. Créer **une issue par phase**, l'assigner à Copilot, relire et fusionner la PR avant de passer à la suivante.

Ordre et dépendances : `0 → 0b → 1 → 2 → 3 → (4 et 5 en parallèle) → 6 → 7 → 8`.
Les sources Bitbucket n'existent plus : la phase 0 reconstruit le dépôt à partir des archives SourceForge.

**Garde-fou commun à toutes les phases** : si la PR dépasse 40 fichiers, ou après 3 échecs consécutifs de la CI, s'arrêter, résumer l'état et poser la question dans la PR. Les règles de `.github/copilot-instructions.md` s'appliquent.

---

## Phase 0 — Reconstruction du dépôt depuis les archives SourceForge

**Contexte** : les archives d'origine (SourceForge, 2009-2015) sont dans la release GitHub `legacy-sourceforge`. Le workflow `copilot-setup-steps.yml` les télécharge dans `$HOME/legacy-archives`, avec un fichier `SHA256SUMS`. L'agent n'a pas besoin d'accéder à SourceForge. Si ce dossier est absent ou vide, s'arrêter et le signaler.

**Objectif** : un dépôt de sources propre, avec la structure d'origine et sans binaires.

**À faire**
1. Inventaire de chaque archive (nom, dossier SourceForge d'origine, date, taille, sha256, contenu de premier niveau) dans `docs/PROVENANCE.md`.
2. Reconstituer l'arbre à la racine du dépôt, un dossier par module **sans suffixe de version** (`sf3jswing-jxakernel-1.1.2` devient `sf3jswing-jxakernel`), d'après les noms de dossiers des archives.
3. Si un module existe en plusieurs versions : garder la plus récente (priorité `Latest/`, puis `Toolkit/`, puis `The Game (SF3JSWING)/`, puis `OldFiles/`). Les autres restent dans la release, pas dans l'arbre. Lister dans `docs/PROVENANCE.md` les fichiers ajoutés ou supprimés d'une version à l'autre.
4. Exclure : `bin/`, `dist/`, `*.jar`, `*.class`, natives (`*.dll`, `*.so`, `*.dylib`), `.keystore` et tout certificat. Lister chaque fichier exclu avec son sha256 dans `docs/PROVENANCE.md`. Garder `LICENSE` et `NOTICE` d'origine. Ajouter un `.gitignore`.
5. Un commit par module : `import: <module> <version> from SourceForge archive <nom>`.
6. Si Maven Central est accessible : signaler dans `docs/PROVENANCE.md` les versions de `net.sf.sf3jswing:*` plus récentes que les archives (ex. noyau 1.3.22 contre 1.1.2 sur SourceForge), sans les importer.

**Acceptation** : arbre de sources sans binaire (`git ls-files` ne contient aucun `.jar`, `.class`, `.dll`, `.so`, `.dylib`) ; sha256 vérifiés ; `docs/PROVENANCE.md` complet. Le mainteneur taguera `baseline-sourceforge` après fusion.

**Interdit** : modifier le moindre fichier source (pas de correction, pas de reformatage), committer des archives, committer un secret.

---

## Phase 0b — Audit (lecture seule)

**Objectif** : produire `docs/AUDIT.md`. Aucune modification de code.

**Contenu attendu** (chaque affirmation avec un chemin de fichier) :
1. Carte des modules et des dépendances (internes et externes, avec versions).
2. Points d'entrée (`main`, Applet) et manière de lancer chaque outil.
3. Build : commandes Ant exactes, cibles, prérequis, natives par OS/architecture, et ce qui échoue aujourd'hui sous JDK 8 sur Ubuntu.
4. Lignes de code par module (`cloc`), tests existants (`jxatest-unit`), nombre de TODO/FIXME.
5. Inventaire de l'existant, avec état (complet / partiel / ébauche) pour : réseau et multijoueur, manettes (JInput/DirectInput), IA, contrôle des personnages animés (états, transitions).
6. Dette de déterminisme : usages de `System.currentTimeMillis/nanoTime`, `Math.random`/`Random`, `float`/`double` dans la simulation, threads partagés, boucle de rendu couplée à la logique.
7. Secrets et gros fichiers dans l'historique (si un outil est indisponible, le dire).
8. Risques classés et effort estimé (S/M/L) pour chaque phase suivante.

**Acceptation** : `docs/AUDIT.md` complet ; aucun fichier modifié hors `docs/`.

---

## Phase 1 — Build reproductible, CI et jar de test

**Objectif** : compiler et lancer le projet sous OpenJDK 8 sur Linux, Windows et macOS, et publier une version de test.

**À faire**
- Conserver Ant. Ajouter un `pom.xml` parent consommant `net.sf.sf3jswing:*` depuis Maven Central et le fork LWJGL (release GitHub `v.2.9.3-jxa`). Documenter ce qui ne peut pas venir de Maven.
- Workflow `.github/workflows/build.yml` : matrice 3 OS, JDK 8 (Temurin), artefacts jar + zip portable par OS, scan de secrets.
- Smoke test headless : démarrage du noyau, chargement d'un personnage d'exemple, 600 frames de combat sans exception.
- Release de pré-publication `test-0.9.x` : jars + zip + notes, combinant éléments Maven, sources du dépôt et archives SourceForge « legacy ».
- `docs/BUILD.md` : comparaison du jar compilé avec l'archive SourceForge (classes différentes).

**Acceptation** : CI verte sur les 3 OS ; `java -jar` lance le jeu (ou message clair en headless) ; artefacts téléchargeables.

**Interdit** : committer des natives, renommer, passer à Gradle.

---

## Phase 2 — Stabilisation

**Objectif** : corriger ce que l'audit et la CI révèlent, dans cet ordre : build/exécution, puis stabilité graphique (Java2D puis LWJGL), puis fuites de ressources et de threads.

**À faire**
- Si JAI bloque (artefact introuvable ou obsolète) : l'isoler derrière une interface (ex. `ImageCodec`) avec un repli `ImageIO`. Ne pas supprimer JAI sans accord.
- Tests de non-régression : charger les exemples d'origine (personnages, niveaux, sprites).

**Acceptation** : tests verts ; script de démo de 10 minutes en rendu logiciel sans exception.

---

## Phase 3 — Simulation déterministe (prérequis du réseau et de l'IA)

**Objectif** : une simulation reproductible à l'identique.

**À faire**
- Séparer la boucle logique (pas fixe, ex. 60 Hz) du rendu.
- Injecter `Clock` et `RandomSource` (graine) : plus de temps réel dans la logique.
- Snapshot/restore de l'état des combattants et hash d'état par frame.
- Replays : enregistrer les entrées par frame et rejouer. Même hash attendu (tests « golden master »).
- Documenter les usages de `float`/`double` dans la simulation. Si une conversion en point fixe est nécessaire et invasive, demander avant.

**Acceptation** : un replay de 1 000 frames donne le même hash sur Linux, Windows et macOS en CI.

---

## Phase 4 — Entrées et manettes

**Objectif** : supporter XInput et DirectInput sans casser le clavier ni JInput.

**À faire**
- Interface `InputDevice` (axes, boutons, hat, déconnexion à chaud) et `InputMapper` configurable par fichier.
- Backends : clavier, JInput existant (DirectInput), XInput (Windows). Linux et macOS via JInput.
- **Avant d'implémenter XInput** : proposer dans la PR 2 bibliothèques (licence compatible Apache 2.0, maintenance, natives) et attendre la décision.
- Périphérique simulé pour les tests ; interface de remappage minimale.

**Acceptation** : tests avec périphérique simulé ; `docs/INPUT.md` avec checklist de test manuel sur manette physique (non testable par l'agent).

---

## Phase 5 — Joueur IA

**Objectif** : intégrer et valider l'IA existante.

**À faire**
- Interface `FighterController` unifiant la source des commandes (humain, IA, réseau, replay). Encapsuler l'algorithme IA existant sans le réécrire.
- Runner headless IA contre IA (graine fixe) avec rapport : victoires, durée, coups. Seuil de régression en CI.
- `docs/CHARACTER-CONTROL.md` : sémantique de contrôle des personnages (états, transitions, priorités) déduite du code. Signaler les ambiguïtés au lieu de les trancher.

**Acceptation** : 100 combats IA contre IA reproductibles, sans exception.

---

## Phase 6 — Multijoueur pair-à-pair

**Objectif** : jeu à deux sans serveur central (esprit d'origine du projet).

**À faire**
- D'abord décrire l'existant (audit), ne rien réécrire sans l'avoir justifié.
- Étape A : lockstep avec délai d'entrée, UDP (`java.nio`), connexion directe IP:port, détection de désynchronisation par hash d'état.
- Étape B (si A est stable) : rollback ; aide NAT optionnelle (STUN) ; relais optionnel et séparé, jamais obligatoire.
- Simulateur réseau (latence, gigue, perte) pour les tests. Le mode hors ligne reste inchangé.
- Sécurité : paquets bornés et validés, pas de désérialisation d'objets Java.

**Acceptation** : deux instances en local, latence simulée 100 ms et 5 % de perte, 10 minutes sans désynchronisation ; `docs/NETPLAY.md`.

---

## Phase 7 — Outils, packaging, Applet

**Objectif** : outils utilisables sans navigateur, livrables installables.

**À faire**
- Éditeurs personnage/niveau et navigateurs (`imcbrowser`, `animsbrowser`) lancés en fenêtre desktop.
- L'Applet sort du build par défaut (profil `applet` optionnel, sources conservées).
- Lanceurs `.sh`, `.bat`, `.command`, zip portable, dossier `samples/` (personnage et niveau d'exemple).
- `docs/TOOLS.md` : créer un personnage, créer un niveau, les tester dans le jeu.
- Optionnel : paquets `.deb`/`.rpm`/`.msi` via `jpackage` (JDK 17 ou plus, uniquement comme outil de build).

**Acceptation** : depuis un zip neuf, créer et lancer un personnage d'exemple en suivant `docs/TOOLS.md`.

---

## Phase 8 — Étude mobile (sans réécriture)

**Objectif** : mesurer l'effort d'un portage Android et iOS tout en Java.

**À faire**
- Module `engine-core` en Java pur : aucun import `java.awt`, `javax.swing` ni `org.lwjgl`, vérifié en CI.
- Prototype libGDX desktop + Android affichant un combat IA contre IA.
- iOS : étude de faisabilité seulement (MobiVM : prérequis, effort, limites). Pas d'implémentation.
- `docs/MOBILE.md` : dépendances AWT/LWJGL à découpler, classées par effort, avec estimation en jours.

**Acceptation** : prototype exécutable sur desktop et émulateur Android ; `docs/MOBILE.md` complet.
