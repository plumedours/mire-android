# Validation de cette livraison

> Livraison actuelle 1.4.3 : correctif de configuration release basé sur le Logcat du Galaxy S21. Le build est bloqué ici avant compilation par le téléchargement réseau du Gradle Wrapper. Les résultats des versions précédentes ci-dessous ne constituent pas une validation du nouvel APK sur appareil.

Projet préparé le 16 octobre 2026 à partir du squelette Mire fourni et du dépôt Volée examiné dans cette conversation.

## Contrôles exécutés

- Compilation de l’ensemble des sources Kotlin métier, stockage, exports et interface avec le compilateur Kotlin et le plugin Compose : **réussite**.
- Ce contrôle a utilisé Kotlin/Compose compiler 2.3.21, des bibliothèques AndroidX réelles et les stubs Android API 36 disponibles dans l’environnement. Il ne remplace pas la compilation Gradle avec les versions exactes du squelette (Kotlin 2.2.10, AGP 9.4.1 et API 37).
- Compilation et liaison des ressources XML Android avec **AAPT2 36**, sur les ressources du projet : **réussite**.
- **69 tests JVM : réussite**, couvrant le moteur de score, les frontières d’anneaux et cordons, les transformations de spots, le recentrage trispot, la conversion Volée, la conservation des scores et des relations, les aller-retours JSON, la fusion/restauration, les erreurs d’import, les objectifs et les exports CSV/XLSX.
- L’algorithme de diamètre de groupe est comparé à une recherche exhaustive sur 40 jeux aléatoires de 70 impacts.
- Le classeur XLSX produit par le code de production a été relu avec **openpyxl** : noms des deux feuilles, accents, score, coordonnée négative et cellule commençant par `=` conservée comme texte vérifiés.
- Sources formatées avec ktfmt ; archive contrôlée et nettoyée des caches, chemins machine et fichiers de compilation.

## Limites des vérifications

La génération complète de l’APK par Gradle n’a pas abouti dans cet environnement : le processus Gradle n’a pas pu joindre les dépôts de dépendances. Aucune APK installable n’est fournie et aucune compilation Gradle complète réussie n’est revendiquée.

La génération du code Room par KSP, les tests instrumentés, les exports PDF/PNG en exécution Android, l’affichage réel, les gestes, les sélecteurs de fichiers et la version release doivent être vérifiés dans Android Studio sur appareil/émulateur. Les tests instrumentés correspondants sont inclus. Les tests de persistance utilisent des bases temporaires distinctes du carnet utilisateur.

L’import a été testé avec des sauvegardes représentatives synthétiques construites d’après le code Volée. Aucune sauvegarde personnelle réelle n’était jointe à cette demande ; les quantités et résultats de l’historique de l’utilisateur restent donc à comparer après import.

## Commandes de vérification locale

Dans le dossier du projet, sous Windows :

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
```

Le dernier contrôle nécessite un appareil. La liste de recette fonctionnelle figure dans `RECETTE.md`.

## Correction après le premier essai Android Studio

La capture du premier essai montrait `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin`, sur les dossiers générés par KSP. Le réglage `android.disallowKotlinSourceSets=false` a été ajouté au fichier `gradle.properties` du projet pour autoriser cet enregistrement par KSP avec les versions conservées. Kotlin intégré reste activé. Cette correction de configuration doit être confirmée par une nouvelle synchronisation et compilation sur le poste Android Studio ; elle ne constitue pas une validation complète de l’APK.

## Livraison 1.1 — thème, blasons et gestes

Le contrôle Kotlin/Compose a été refait sur toutes les sources, ainsi que la validation AAPT2 des ressources. Les 56 tests JVM incluent le catalogue étendu, les zones 5–10, les grilles à 4 et 12 spots, les silhouettes et barèmes nature/3D, le centre campagne, les sauvegardes nouvelles/anciennes et les transformations de vue. Deux tests instrumentés de gestes (glisser-relâcher et zoom à deux doigts sans impact) ont été ajoutés ; ils nécessitent un appareil et ne sont pas présentés comme exécutés ici.

La réactivité tactile, le confort de la loupe et le rendu réel du nouveau thème restent à confirmer sur téléphone ou émulateur. La compilation APK complète Gradle/KSP n’a pas été exécutée avec succès dans l’environnement de préparation. Le paramètre de compatibilité KSP signalé lors du premier essai est conservé.


## Livraison 1.2 — vital neutre, catalogue et palettes

Compilation Kotlin/Compose de toutes les sources de production : réussite. Compilation et liaison AAPT2 : réussite. Les **63 tests JVM** passent, dont sept nouveaux tests couvrant le regroupement du catalogue, la conservation des cibles personnalisées, les limites des zones neutres, les barèmes Nature, le transport des paramètres de séance, l’aller-retour des coordonnées historiques, les sauvegardes natives 1/2/3 et la géométrie du petit disque ballon avec cordon.

Ces contrôles ont les mêmes limites que ceux décrits plus haut : pas de build APK Gradle/KSP complet abouti ni d’exécution sur émulateur ou téléphone dans cet environnement. Le rendu des nouvelles palettes, le catalogue et le confort tactile doivent encore être vérifiés dans Android Studio. Aucun fichier APK n’est livré.


## Livraison 1.2.1 — loupe fixe et croix décalée

Compilation de toutes les sources Kotlin/Compose de production : réussite. **67 tests JVM réussis**, dont quatre nouveaux contrôles de la visée au-dessus du doigt, du blocage aux bords, du décalage constant après zoom/déplacement de vue et de la correspondance entre croix et impact enregistré. Les tests de loupe existants vérifient désormais sa hauteur fixe et son blocage horizontal.

Un test instrumenté de débordement du canevas a été ajouté, et le test de glisser-relâcher tient compte du point de visée décalé. Ces tests instrumentés n’ont pas été exécutés ici. Le confort tactile et l’affichage sur appareil restent à vérifier, ainsi que le build APK Gradle/KSP complet ; aucune APK installable n’est fournie.


## Livraison 1.3 — suivi continu, icône et écran de lancement

- Les 67 tests JVM passent après adaptation des tests de loupe : suivi horizontal et vertical, décalage continu au-dessus de la croix, blocage horizontal, absence de saut à la ligne médiane et au bord supérieur. Les tests parcourent le bord haut et le bord bas pixel par pixel, y compris hors canevas.
- Compilation et liaison de toutes les ressources Android avec AAPT2 : réussite, y compris le PNG, les icônes adaptatives, la variante monochrome et les ressources de lancement. Les identifiants R utilisés par le contrôle Kotlin proviennent de cette liaison AAPT2.
- L’icône est un PNG RGBA 1254 × 1254 avec canal alpha de 0 à 255 ; le fichier généré est copié sans retouche dans les ressources du projet.
- Les limites précédentes restent applicables : aucune APK Gradle/KSP complète ni installation sur appareil n’est validée ici. L’animation, la zone de débordement de la loupe, les masques du lanceur et les comportements de démarrage système restent à vérifier avec la recette sur appareil.


## Livraison 1.4 — mise en page compacte et lancement unifié

- Compilation Kotlin/Compose de toutes les sources de production : réussite, avec les bibliothèques AndroidX réelles et les identifiants R générés par AAPT2. Les versions et limites du contrôle manuel décrites au début de ce document restent applicables.
- Compilation et liaison des ressources avec AAPT2 : réussite, avec le nouveau PNG, les calques adaptatifs, l’icône système transparente et les thèmes Android.
- **69 tests JVM réussis**, dont deux tests supplémentaires sur l’adaptation de la loupe près du haut de la fenêtre et la continuité de sa position et de son rayon aux limites. Les anciens tests de position, score et sauvegarde restent verts.
- Transparence PNG RGBA vérifiée (alpha 0–255). Le rayon des pixels d’alpha supérieur à 16 dans le calque adaptatif est de 32,25 dp, inférieur au rayon de sécurité de 33 dp avec les marges choisies.
- Un test instrumenté vérifie l’absence de zone réservée avant le canevas, et un autre vérifie la visibilité et la fin unique de l’introduction de 2,3 secondes. Ils sont inclus, mais n’ont pas été exécutés ici.

Aucun essai sur appareil ou émulateur et aucun build APK complet Gradle/KSP abouti ne sont revendiqués. La fenêtre flottante de loupe, ses interactions avec le défilement, le comportement réel du lanceur et du lancement Android doivent être vérifiés dans Android Studio. La fenêtre système de démarrage n’est pas supprimée : elle reçoit un fond uni et un drawable transparent, puis est retirée dès que l’app dessine sa première image.


## Livraison 1.4.1 — suppression et réouverture des séances

Compilation de toutes les sources Kotlin/Compose de production : réussite. Les 69 tests JVM existants passent. Les ressources Android sont inchangées par rapport à la version 1.4.

Deux tests instrumentés ont été ajoutés pour vérifier que la corbeille exige une confirmation, qu’Annuler ne supprime rien, que les actions ne déclenchent pas aussi l’ouverture de la carte, et que Rouvrir utilise son action dédiée. Ils ne sont pas présentés comme exécutés ici : aucun appareil ni émulateur n’a été utilisé. La recette inclut la persistance de la suppression et la conservation des flèches à la réouverture. Les limites Gradle/KSP/APK décrites plus haut restent applicables.


## Livraison 1.4.2 — icônes vectorielles Material

Compilation Kotlin/Compose de toutes les sources de production : réussite. Compilation et liaison AAPT2 des ressources Android : réussite. Les tracés visibles des trois VectorDrawable ont été comparés aux SVG Google téléchargés et sont strictement identiques. Les SVG source et la licence sont inclus. Aucune dépendance n’a été ajoutée et aucune logique métier n’a été modifiée.

Cette correction d’icônes n’a pas donné lieu à une nouvelle exécution des tests JVM métier. Le rendu sur appareil et les tests instrumentés restent à vérifier, avec les mêmes limites Gradle/KSP/APK que les livraisons précédentes.


## Livraison 1.4.3 — IllegalAccessError dans l’APK release

Le journal utilisateur situe l’erreur dans le provider AndroidX Startup et ProcessLifecycleInitializer.dependencies, avant MainActivity. L’optimisation partielle R8 est suspectée en raison des classes Kotlin renommées et de la configuration release. Elle est désactivée et packageScope est retiré.

La propriété utilisée a été vérifiée dans l’API officielle AGP 9.4. Les contrôles statiques confirment optimization.enable=false, l’absence de packageScope dans le module, le versionCode 9 et la conservation du package applicatif. Le Gradle Wrapper a été lancé pour assembleRelease ; l’essai échoue avant configuration/compilation sur java.net.SocketException: Network is unreachable pendant le téléchargement de Gradle 9.6.0. Aucun build complet ni essai S21 réussi ne peut donc être annoncé. Les tests métier ne sont pas réexécutés pour ce changement de configuration.

La recette de validation doit porter sur l’APK release nouvellement reconstruite, signée avec la clé utilisateur, installée en mise à jour sur le même S21. Un lancement debug réussi ne suffirait pas à valider cette correction.
