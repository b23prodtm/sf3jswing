# Audit technique SF3JSwing

Audit de phase 0b, 2026-10-24. Il s’agit d’un état des lieux en lecture seule des imports SourceForge et des descripteurs de build présents dans le dépôt. Les références sont relatives au dépôt et pointent dans `legacy-sourceforge/`; les dossiers versionnés en double reflètent la structure des archives. Aucun fichier source n’a été modifié pour cet audit.

## 1. Périmètre et état du dépôt

- Les sources des projets sont dans `legacy-sourceforge/<archive>/<project>/`, pas à la racine. Il n’y a pas de `build.xml` ou `pom.xml` racine, ni de workflow de build/test; le seul workflow est `.github/workflows/copilot-setup-steps.yml`. Le `README.md` racine comporte encore des sections Installation, Utilisation et Licence à compléter.
- Huit arborescences d’archives sont présentes : six modules à la version fournie la plus récente et deux snapshots plus anciens (`sf3jswing-jxakernel` 1.3.5 et `sf3jswing-jigaxtended` 4.6.7). Les templates du framework référencent `sf3jswing-animsbrowser-ant`, absent des archives/sources importées. Un template référence aussi `es-sdk-jdk-binding`, qui ne figure pas dans la liste des phases/modules.
- Les archives récupérées par le workflow dans `$HOME/legacy-archives` ont été vérifiées avec `SHA256SUMS`; les huit sommes correspondent. Le script de récupération et de génération des sommes est `.github/workflows/copilot-setup-steps.yml`.
- Avant l’ajout de ce rapport, le dépôt comptait 2 072 fichiers suivis, dont 380 fichiers Java. Aucun `.jar`, `.class`, `.dll`, `.so` ou `.dylib` n’est suivi par Git. Plusieurs fichiers non-source dépassent 1 Mio : un WAV d’exemple dans `sf3jswing-jxatest-unit` et des index Xcode dans `sf3jswing-thegame-ant`. Ces derniers sont des artéfacts IDE archivés, pas des entrées de build.
- L’historique disponible correspond à la reconstruction/import récente, pas à l’historique Bitbucket/SourceForge perdu. Ces commits ne permettent donc pas de certifier l’absence de secrets ou de fichiers volumineux dans l’historique d’origine; cette perte est aussi mentionnée dans `docs/COPILOT-PHASES.md`.

## 2. Modules, dependencies et taille

La mesure ci-dessous est le nombre brut de lignes de chaque fichier `*.java` dans l’arborescence d’archive sélectionnée, templates/exemples et lignes vides/commentaires compris. `cloc` n’est pas installé : ces valeurs ne sont donc pas des LOC `cloc`. `TODO/FIXME` compte les occurrences littérales dans les mêmes fichiers Java.

| Module (archive sélectionnée) | Rôle | Java / lignes brutes / TODO-FIXME | Dépendances ou références principales |
|---|---|---:|---|
| `sf3jswing-jxakernel` 1.3.6 | Noyau : environnement, cache, extensions, UI `JFCFrame`/`JFCApplet`. | 41 / 17 147 / 2 | Modules dépendants; les archives/descripteurs citent Commons Codec 1.3, HttpClient 3.1, Commons IO 2.1, HttpCore 4.0, JUnit-dep 4.6 et Ant-Contrib 1.0b3. |
| `sf3jswing-jigaxtended` 4.6.8 | Framework de jeu, modèles/animations, contrôles, Java2D/LWJGL. | 112 / 46 652 / 5 | Dépend du noyau. Les archives citent JAI, JInput, LWJGL, OpenAL, JMF 2.1.1e et SwingX 0.9.5 (Linux; d’autres versions SwingX apparaissent selon la plateforme). Les noms de jars LWJGL et JInput ne précisent pas leur version. |
| `sf3jswing-thegame-ant` 0.9.10 | Jeu SF3, joueurs humain/machine/distant, dialogues de connexion. | 39 / 11 251 / 20 | Dépend de JXA/Jigaxtended; le descripteur Linux référence Jigaxtended 4.4.0 alors que la source la plus récente fournie est 4.6.8. Le build référence aussi une ressource MySQL Connector/J 3.0.17. |
| `sf3jswing-jxatest-unit` 0.2.0 | Exemples/tests interactifs du noyau et du framework. | 4 / 944 / 1 | Dépend de JXA/Jigaxtended; la cible Ant lance `TestAntFrame`/`TestAntApplet`. |
| `sf3jswing-imagemulticonverter-ant` 2.3.9 | Outil de conversion d’images. | 1 / 1 044 / 1 | Dépend de JXA/Jigaxtended et des imports JAI; le build Linux passe par `JFCApplet` et `jxa.ImageMultiConverter_Ant`. |
| `sf3jswing-imcbrowser` 2.5.1 | Navigateur/outil de données et d’images. | 30 / 9 661 / 0 | Dépend du framework; les archives citent Google Data API 1.0, RestFB 1.10.1, JAXB 2.1.9, SwingX et d’autres bibliothèques spécifiques aux plateformes. Les sources importent aussi des API JavaFX et JMF. |

Les noms de bibliothèques ci-dessus viennent des archives et des descripteurs; leurs binaires ne sont pas suivis dans le dépôt actuel. Sources principales : `build.xml`, `build-jars.xml`, `build-Linux-amd64.xml` et `lib/` dans chaque module. Les références de dépendances/version du framework sont dans `sf3jswing-jigaxtended-4.6.8/build-jars.xml`; le parent/version du jeu est dans `sf3jswing-thegame-ant-0.9.10/build-Linux-amd64.xml`. La liste de modules prévue figure dans `docs/COPILOT-PHASES.md`.

Les archives SourceForge conservées contiennent des fichiers `LICENSE`/`NOTICE` et des métadonnées Apache-2.0. Le projet déclare désormais LGPL-3.0-or-later dans `LICENSE`; les fichiers et avis historiques, ainsi que les notices des composants tiers, restent conservés. Lors de toute redistribution, vérifier la portée des licences et garder leurs avis respectifs.

## 3. Points d’entrée et lancement

- `JFCFrame.main`, dans `sf3jswing-jxakernel-1.3.6/src/all/net/sf/jiga/xtended/ui/JFCFrame.java`, est l’hôte desktop des applications JXA `AntFrame`. `JFCApplet` et `AntApplet` fournissent l’ancien hôte Applet. `JFCFrame` peut lancer une classe frame/applet d’un module avec des arguments.
- Jeu : `sf3jswing-thegame-ant-0.9.10/build-Linux-amd64.xml` configure `net.sf.jiga.xtended.ui.JFCApplet` comme hôte et `thegame.Sf3` comme classe AntApplet. `Sf3.java` n’est pas la méthode `main` configurée.
- Convertisseur d’images : la configuration Linux lance `net.sf.jiga.xtended.ui.JFCApplet` avec `jxa.ImageMultiConverter_Ant`; cette classe contient aussi une méthode `main`.
- Navigateur IMC : `build-Linux-amd64.xml` lance `net.sf.jiga.xtended.ui.JFCFrame` avec `-antframe jxa.IMCBrowser`.
- Tests : le module JXA configure `JFCFrame` avec `net.sf.jiga.xtended.ui.test.TestAntFrame`; ses cibles incluent `run-test` et `run-applet-test`.
- `ant -projecthelp` exécuté dans une racine de module liste les cibles disponibles. Le README racine ne donne pas encore d’instructions de lancement. Les navigateurs actuels ne prennent plus en charge les Applets; il faut privilégier une voie desktop.

## 4. Build et tests

### Build Ant trouvé

Chaque `build.xml` de projet importe un fichier spécifique au système/architecture, `build-${os.name}-${os.arch}.xml`; celui-ci importe ou configure `build-jars.xml`. Un descripteur Linux amd64 existe pour chacun des six modules les plus récents. D’autres descripteurs couvrent d’anciennes combinaisons Linux i386, Windows x86/amd64 et Mac PPC/i386/amd64; leur présence ne garantit pas qu’elles compilent ou fonctionnent encore.

Les cibles rapportées par `ant -projecthelp` incluent `compile`, `clean`, `jar`, `run` et `javadoc`; le framework/noyau expose aussi des cibles d’extension/template. Le module de test JXA propose `run-test` et `run-applet-test`. Dans cet environnement (Temurin Java 8 et Ant 1.10.14), `ant -projecthelp` a réussi dans les racines de module extraites. Il est impossible de lancer un build depuis la racine du dépôt, qui n’a pas de descripteur global.

`ant compile` a été essayé dans des extractions temporaires des archives afin de ne pas générer de fichiers de build dans le dépôt :

- Noyau 1.3.6 : `BUILD SUCCESSFUL`.
- Jigaxtended 4.6.8 : `BUILD FAILED`, avec jusqu’à 100 erreurs du compilateur Java, notamment des classes du noyau/framework introuvables.
- Jeu 0.9.10, module de test, navigateur IMC et convertisseur : le compilateur a signalé jusqu’à 100 erreurs par module, mais Ant a terminé par `BUILD SUCCESSFUL`. Leurs tâches `javac` ne définissent pas toutes `failonerror="true"`; les descripteurs du framework et du navigateur le définissent. Le code retour Ant n’est donc pas un indicateur fiable de compilation pour tous les modules.

Ces essais `compile` sont indépendants et ne constituent pas un build multi-module correctement ordonné. Les artéfacts frères/générés manquants et la version du framework incohérente sont probablement liés aux erreurs observées; cet audit ne permet pas d’affirmer qu’un build complet en résoudrait toutes. Aucun jar signé, lancement `run`, rendu graphique, test Applet/JUnit ou build multiplateforme n’a été exécuté. Les cibles `jar` par défaut signent les jars et les descripteurs historiques font référence à un `.keystore` externe; ne pas lancer la signature tant que sa configuration n’est pas explicite et sûre.

Les templates de build incluent des réglages source/target Java 1.5 au lieu du niveau 1.8 requis; la plupart des tâches de compilation ne précisent pas explicitement la version du langage. À corriger et vérifier dans la phase build. Plusieurs configurations de lancement ont aussi des options JVM obsolètes et supposent la présence des Java Extensions/Applets.

### Tests

L’archive de test contient quatre classes Java de test/démonstration (`TestModel`, `TestGameActionLayer`, `TestAntFrame`, `TestAntApplet`), mais aucune annotation JUnit ni classe `junit.framework.TestCase` n’a été trouvée. Ce sont des exemples GUI/rendu, pas une suite de non-régression headless. La présence de `junit-dep-4.6.jar` dans l’archive du noyau ne prouve pas que ce module exécute des tests JUnit.

Le workflow de setup installe Temurin 8, Ant, Xvfb et `libxxf86vm-dev`, puis télécharge les archives; il ne compile ni ne teste les modules. Le rendu OpenGL, les manettes physiques, le jeu en réseau et les tests GUI restent non vérifiés.

## 5. Fonctionnalités existantes

| Sujet | État | Éléments constatés |
|---|---|---|
| Réseau / multijoueur | **Partiel / ébauche** | Des classes client/serveur TCP `Socket`/`ServerSocket` existent (`ClientConnectionDialog`, `ServerConnectionDialog`, `RemotePlayer`, `RemoteAlgorithm`, `ServerAlgorithm`). Les classes distantes contiennent encore des TODO et des commentaires signalant des opérations incomplètes. `RemoteAlgorithm` construit un `ObjectInputStream` sur une socket et lit des `GameProperties`/`Game` sérialisés; cela contrevient à l’exigence de sécurité réseau et doit être traité avant d’utiliser le jeu en réseau. Aucun lockstep, replay, rollback, bornage de paquets ou hash de désynchronisation n’a été trouvé. |
| Clavier / manettes | **Partiel** | `ControllersHandler` de Jigaxtended importe JInput `Controller`, interroge axes/boutons et associe les événements aux touches des joueurs; le clavier passe par AWT. Les configurations natives nomment DirectInput/JInput. Aucun backend spécifique XInput ni mapper configurable n’a été trouvé. |
| IA | **Ébauche / partiel** | `MachinePlayer`, `Fighter`, `ServerAlgorithm` et `AlgorithmInterface` existent. Le joueur machine active actuellement le mode machine de `Fighter`; qualité des décisions, stabilité des combats automatisés et déterminisme n’ont pas été testés. Aucune interface unifiée `FighterController` ni runner IA-vs-IA headless n’a été trouvée. |
| Contrôle des personnages animés | **Partiel** | Le framework comporte `Animation`, `AnimationHandler`, `InteractiveModel`, `XtendedModel`, des outils modèle/sprite et le code `Fighter`. Le contrôle/animation est réparti entre framework et jeu; aucun contrat documenté unique des états/transitions/priorités ni tests de référence n’a été trouvé. `TestModel` est un exemple interactif, pas un test headless de compatibilité. |

Sources représentatives : `sf3jswing-thegame-ant-0.9.10/src/all/thegame/{RemoteAlgorithm,RemotePlayer,ServerAlgorithm,MachinePlayer}.java`, `.../comp/Fighter.java`; et `sf3jswing-jigaxtended-4.6.8/src/all/net/sf/jiga/xtended/{impl/Animation.java,impl/AnimationHandler.java,impl/system/input/ControllersHandler.java,impl/game/InteractiveModel.java,impl/game/XtendedModel.java}`.

## 6. Dette de déterminisme

Aucun `Clock` injecté, `RandomSource` à graine contrôlée, snapshot/restore, hash d’état par frame ou système de replay n’a été trouvé. `ServerAlgorithm.java` indique un tick de 10 ms; `FieldGui`, `RenderingScene` et `GameActionLayer` répartissent UI/rendu/tâches entre plusieurs classes, sans séparation démontrée entre simulation à pas fixe et rendu.

À titre indicatif, des recherches littérales dans les six modules les plus récents ont trouvé environ 45 occurrences de `System.currentTimeMillis`, 87 de `System.nanoTime`, 1 de `Math.random`, 50 de `new Random(`, 867 de `float` et 448 de `double`. Ce sont des occurrences lexicales, pas l’affirmation qu’elles touchent toutes la simulation. Exemples : `Chrono.java`, `ControllersHandler.java`; `RandomComparator.java` du jeu utilise `Math.random`. Des threads et files d’événements apparaissent aussi dans le framework/jeu. Il faut identifier les usages liés à la simulation et les responsabilités inter-threads avant de modifier la représentation numérique.

## 7. Secrets, fichiers volumineux et historique

- Aucun suffixe de bibliothèque native ou binaire Java (`.jar`, `.class`, `.dll`, `.so`, `.dylib`) n’est suivi dans ce checkout. Les chemins de ressources natives figurent dans les propriétés/descripteurs, mais les binaires correspondants ne sont pas dans l’arbre suivi. Les descripteurs attendent des bibliothèques LWJGL/JInput/OpenAL/JAI/JMF propres à chaque plateforme.
- Les descripteurs Ant historiques contiennent des paramètres de signature en clair et font référence à un keystore externe. Les valeurs ne sont volontairement pas reproduites ici. Considérer ces paramètres comme exposés, les supprimer des configurations de build/release à venir et ne conserver la signature qu’avec un secret protégé. Aucun fichier keystore n’est suivi.
- L’historique récent de reconstruction est disponible, mais pas l’historique d’origine. Cet audit ne peut donc pas certifier l’absence de secrets dans l’historique Git antérieur à la reconstruction. Le workflow de setup utilise le token fourni par GitHub pour télécharger une release; aucun token en clair n’a été trouvé dans le workflow.
- Parmi les fichiers de plus de 1 Mio figurent le WAV d’exemple du module de test et plusieurs artéfacts d’index Xcode dans l’arborescence du jeu. Ils sont suivis alors qu’ils ne sont pas nécessaires à la compilation Java; vérifier leur provenance et utilité sans supprimer les exemples historiques avant d’avoir clarifié la compatibilité attendue.
- `cloc` et les outils dédiés au scan de secrets dans l’historique n’étaient pas disponibles. Les nombres Java ci-dessus sont des lignes brutes; l’analyse de l’historique est limitée aux commits disponibles.

## 8. Risques et effort des phases suivantes

Effort estimé qualitativement selon l’ampleur et les dépendances visibles; il ne s’agit pas d’une estimation calendaire.

| Phase | Risque principal | Niveau | Effort |
|---|---|---|---|
| 1 — Build reproductible / CI | Build multi-module incomplet, réglages Java 1.5/implicites, dépendances anciennes/non versionnées, tâches Ant pouvant masquer des erreurs, configuration de signature dangereuse. | Élevé | L |
| 2 — Stabilisation | JAI/JMF/JavaFX, rendu Java2D/LWJGL, ressources et threads; exécution réelle pas encore vérifiée. | Élevé | L |
| 3 — Déterminisme | Horloges/hasard/threads, `float`/`double`, logique répartie entre Fighter/algorithme/rendu. | Élevé | L |
| 4 — Entrées et manettes | JInput existe mais XInput n’existe pas; sélectionner toute nouvelle dépendance nécessite validation préalable de licence/maintenance/natives. | Moyen | M |
| 5 — IA | Existant non couvert par tests reproductibles; intégration multi-contrôleurs à définir. | Moyen | M |
| 6 — Multijoueur | Code réseau ébauché, Java serialization reçue par socket, pas de protocole borné ni de désynchronisation mesurée. | Critique | L |
| 7 — Outils et packaging | Outils liés à JXA/Applet/Swing et dépendances historiques; lanceurs multi-OS et exemples à valider. | Moyen | M |
| 8 — Étude mobile | AWT/Swing/LWJGL fortement présents; découplage du noyau potentiellement transversal. | Élevé | L |

### Priorité proposée

1. Établir un build de bout en bout vérifiable sans exécuter la signature intégrée, mettre au clair les dépendances/versions et faire échouer Ant sur toute erreur Java.
2. Corriger le socle de compilation et tester le rendu Java2D avant les changements fonctionnels.
3. Cartographier les accès temps/aléa/thread liés à la simulation; ne pas entreprendre une conversion numérique invasive sans accord.
4. Sécuriser l’échange réseau et définir des tests de paquets bornés avant tout travail de rollback.
5. Continuer vers entrées, IA, packaging et mobile suivant l’ordre de `docs/COPILOT-PHASES.md`.
