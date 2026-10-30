# Mire 1.4.3 — Carnet de tir à l’arc

Application Android native en Kotlin / Jetpack Compose / Material 3, conçue pour fonctionner hors ligne. Identifiant : `com.plumedours.mire`, Android 8.0 minimum (API 26). Mire peut être installée à côté de Volée.

## Ouvrir le projet

1. Décompresser l’archive dans un dossier de développement, par exemple `C:\Dev\Mire`.
2. Dans Android Studio, choisir **Open**, puis le dossier qui contient `settings.gradle.kts`.
3. Laisser Android Studio recréer `local.properties` et synchroniser Gradle. Le premier démarrage nécessite une connexion pour télécharger les dépendances.
4. Installer le SDK Android demandé par le projet (API 37 du squelette fourni). Conserver le JDK intégré compatible sélectionné par Android Studio ; le squelette contient des critères de daemon Java 25.
5. Sélectionner un téléphone ou un émulateur, puis **Run ▶**.

Ne pas copier les fichiers Kotlin un à un dans l’ancien projet : ouvrir ce dossier complet. Le Gradle Wrapper et les configurations du squelette sont inclus. Aucun chemin SDK propre à un ordinateur, cache Gradle, clé de signature ou jeu de données personnel n’est livré.

## Utilisation

- **Accueil** : séances récentes, séances à reprendre, activité sur 30 jours, nouvelle séance.
- **Séances** : recherche, filtres de statut/pratique/cible, historique et exports.
- **Suivi** : courbe de score relatif, filtres de période/cible/distance/arc, meilleures moyennes par configuration, objectifs et modèles.
- **Matériel** : arcs, jeux de flèches, configurations, archivage et restauration.
- **Réglages** : profil, langues français/anglais/système, thèmes, haptique, écran actif, mode gaucher, catalogue de cibles, imports et sauvegardes.

### Pendant le tir

Toucher puis glisser sur le blason : la loupe suit la visée horizontalement et verticalement, au-dessus de la croix et du doigt, sans basculer ni changer brusquement de côté. La loupe apparaît en superposition pendant la visée, sans espace vide réservé au-dessus de la cible. Son rayon s’adapte progressivement à la place disponible près du haut de l’écran. La croix de visée est décalée de 36 dp au-dessus du doigt, puis bornée aux bords avec une marge de 12 dp pour rester visible. La loupe et le score prévisualisé utilisent cette croix. Relâcher enregistre une seule flèche à la position de la croix. Si le doigt déborde du canevas, la croix reste bloquée au bord et la position bornée est enregistrée au relâchement. Il n’est plus nécessaire d’attendre un appui long. La loupe et le blason réutilisent une couche graphique mise en cache ; les mouvements du doigt ne redessinent pas toutes les formes.

Pour modifier un impact, ouvrir sa pastille, choisir **Déplacer sur la cible**, puis glisser et relâcher. Les flèches proches ne sont plus sélectionnées automatiquement : un groupement serré ne provoque pas le déplacement involontaire de la flèche précédente.

Pincer avec deux doigts permet de zoomer et déplacer la cible, sans ajouter une flèche. Les boutons **+ / −** restent disponibles ; toucher la valeur du zoom réinitialise la vue. Les coordonnées restent indépendantes du zoom et de la taille d’écran.

Les boutons de score permettent une saisie sans position. Ces flèches comptent dans le score et le volume, mais n’entrent pas dans les statistiques géométriques. **Annuler flèche** supprime la dernière flèche de la volée courante. Les autres flèches sont modifiables depuis leur pastille.

Le catalogue visuel et les raccourcis de l’accueil permettent d’accéder directement aux blasons classiques, trispots, campagne, animaux et ballon.

Le tir 3D et le mode Nature utilisent un schéma neutre de contours du vital, sans silhouette ni espèce d’animal. Les zones sont dessinées dans le moteur vectoriel commun à la saisie et aux exports. Le tir 3D propose `11/10/8/5`, `12/10/8/5` ou `10+/10/8/5`. Ces schémas ne représentent pas les dimensions d’une cible de fabricant ; les scores restent corrigibles avec les boutons. Aucune mesure physique de groupement n’est calculée sur ces schémas.

Le mode nature utilise deux flèches : première flèche 20/15/0, seconde 15/10/0. Les zones sont distinguées dans l’aperçu et les boutons. Les parcours conservent le nombre de cibles, tours, repères et distances.

Le mode ballon affiche un petit disque jaune uni sur fond bleu contrasté, sans ficelle ni illustration. Son diamètre nominal vaut 5 cm par défaut et reste configurable. Il possède deux grandes touches **Touché / Manqué**, le placement graphique, un diamètre configurable et un taux de réussite. Le Beursault possède un dessin monochrome, la saisie manuelle 4/3/2/1/0 et les compteurs d’honneurs et chapelets ; les cordons sont confirmés par l’utilisateur, sans calcul automatique sur un dessin schématique.

**Volée suivante** valide également une volée incomplète si nécessaire. Le nombre de volées prévues est indicatif, il reste possible de continuer. **Terminer** clôt la séance ; **Rouvrir** dans le résumé permet de la reprendre.

Les impacts sont écrits après chaque action dans une transaction locale. La saisie reprend à la dernière volée sélectionnée après fermeture. La durée utilise le début et la fin de séance : une séance laissée ouverte continue de compter le temps écoulé.

### Résumé et analyses

La première page affiche tous les impacts. Balayer horizontalement, utiliser les flèches ou le sélecteur pour afficher une volée individuelle.

Un trispot peut être affiché :

- dans sa disposition réelle ;
- avec les spots superposés autour d’un centre commun ;
- avec un seul spot sélectionné.

Le recentrage conserve les décalages réels des impacts par rapport au centre de leur spot. Les points ne sont pas recentrés sur la moyenne du groupement. Les positions ambiguës importées et les saisies sans position sont exclues de l’analyse géométrique.

Le rayon moyen décrit la dispersion autour du centre du groupe. Le décentrage décrit le décalage par rapport au point visé. L’écart maximal correspond à la plus grande distance entre deux impacts. Les unités utilisent le diamètre nominal du blason. Les dimensions d’un trispot 40 cm sont donc rapportées au blason complet de 40 cm, et non au diamètre visible du spot réduit.

La carte de chaleur est un affichage indicatif ; au-delà de 2 000 impacts, elle est échantillonnée pour limiter le coût du dessin. Les statistiques numériques utilisent tous les impacts exploitables. Les objectifs géométriques demandent au moins trois positions par séance.

## Importer les données de Volée

1. Garder Volée installée et ouvrir ses réglages.
2. Créer une **sauvegarde JSON**, nommée par exemple `volee_sauvegarde_20261016_1000.json`.
3. Enregistrer le fichier sur le téléphone ou dans un emplacement accessible au sélecteur Android.
4. Dans Mire, choisir **Réglages → Importer Volée ou Mire**.
5. Choisir le JSON, lire le nombre de séances/impacts et les observations.
6. Choisir **Fusionner** pour ajouter les données. Pour un remplacement complet, choisir **Restaurer en remplaçant le carnet**, puis confirmer.
7. Contrôler les quantités, les totaux et quelques séances, dont un trispot, avant de retirer Volée.

Le format pris en charge est `volee-backup`, version `1`, avec les tables du dépôt Volée examiné (base SQLite version 8). Le format JSON et la version de base de données sont deux numéros distincts.

Importés : profil, arcs, types d’arcs, flèches, configurations, cibles, séances, impacts, cibles de parcours, modèles, objectifs et journaux. Les notes et champs de matériel supplémentaires sont conservés. Les préférences d’interface ne font pas partie de la sauvegarde Volée examinée.

Les scores, X et numéros de volée historiques sont conservés sans recalcul. Déplacer un ancien impact dans Mire recalcule uniquement cet impact avec le moteur Mire. Les coordonnées d’origine et l’archive JSON source complète sont conservées dans les sauvegardes Mire.

Pour le trispot vertical Volée, la conversion utilise les centres `0.205 / 0.5 / 0.795` et le rayon visible `0.145` du moteur d’origine. Un spot probable est déduit ; les positions hors spot ou proches d’une frontière entre spots sont marquées à vérifier. Les séances 3D gardent leurs scores et coordonnées source, mais ne reçoivent pas une analyse circulaire inventée. Les dates sans fuseau sont interprétées dans le fuseau du téléphone utilisé pour importer.

**Importer une seule sauvegarde Volée complète, la plus récente.** Le même fichier exact est détecté et bloqué lors d’une seconde fusion. Deux exports Volée différents ont des espaces d’identifiants différents et peuvent contenir des séances communes ; l’aperçu le signale. Pour recommencer depuis un autre export complet, sauvegarder d’abord Mire puis utiliser le remplacement.

Pour une sauvegarde Mire, la fusion conserve les enregistrements existants ayant le même identifiant. Utiliser la restauration pour rétablir exactement les données d’un ancien carnet. Les préférences d’interface du téléphone courant sont conservées lors des imports et restaurations.

La limite d’import est de 50 Mio. Toute validation ou écriture refusée laisse les données précédentes intactes.

## Sauvegardes et exports

- **JSON** : sauvegarde complète et versionnée, destinée à la restauration.
- **PDF** : rapport au format Lettre avec cible, résultats, détail des flèches et journal paginé.
- **Excel `.xlsx`** : deux feuilles, séances et impacts.
- **CSV UTF-8** : détail de tous les impacts, séparateur point-virgule.
- **PNG** : carte de résultat enregistrable ou partageable avec le sélecteur Android.

La sélection d’un emplacement d’enregistrement utilise le sélecteur Android, sans permission d’accès global au stockage. Les données restent locales ; les sauvegardes automatiques Android sont désactivées pour ce carnet. Créer régulièrement un JSON et le conserver ailleurs que dans les données privées de l’application. La désinstallation supprime la base locale.

## Développement

Les versions AGP, Gradle, Kotlin, Compose et SDK du squelette ont été conservées. Les ajouts incluent Room 2.8.4 et KSP 2.2.10-2.0.2. Le résolveur Foojay optionnel a été retiré des settings ; utiliser le JDK d’Android Studio.

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
```

Les deux premières commandes ne nécessitent pas de téléphone. La troisième nécessite un appareil ou un émulateur. Pour une APK de distribution, utiliser **Generate Signed App Bundle / APK**, créer une clé et la conserver en dehors du dépôt.

Consulter [docs/ARCHITECTURE.md](ARCHITECTURE.md), [docs/VALIDATION.md](VALIDATION.md) et [docs/RECETTE.md](RECETTE.md) avant de diffuser une version.

## Nouveau dépôt

Créer un dépôt GitHub vide `plumedours/mire-android`, puis exécuter dans ce dossier :

```powershell
git init
git branch -M main
git add .
git commit -m "Implémente le carnet de tir à l’arc Mire"
git remote add origin https://github.com/plumedours/mire-android.git
git push -u origin main
```

Le dépôt distant n’a pas été créé ou modifié par la préparation de cette archive.

## Mise à jour depuis Mire 1.0

Ouvrir le nouveau dossier complet dans Android Studio et réinstaller avec **Run**, sans désinstaller Mire du téléphone/émulateur. L’identifiant reste `com.plumedours.mire`. Les nouvelles définitions de cibles sont ajoutées au catalogue existant sans remplacer les définitions personnalisées ni les séances passées. Le thème Sport bleu nuit est activé une seule fois lors de la migration du design ; les autres thèmes restent sélectionnables.

Les nouvelles sauvegardes Mire utilisent la version JSON 3 ; les sauvegardes Mire versions 1 et 2 et Volée version 1 restent importables. La structure physique Room est conservée et aucune remise à zéro de base n’est effectuée.

Voir `docs/NOUVEAUTES-1.1.md` pour les détails et les limites de validation.


## Mire 1.4.3 — modèles et paramètres

Le sélecteur regroupe désormais le catalogue en **11 modèles intégrés**, plus les cibles personnalisées : blason couleurs, trispot vertical, trispot triangle, quatre blasons, campagne, Worcester, 3D, Nature, Beursault, ballon et paille. Les anciennes variantes n’étaient pas toutes identiques : elles changeaient le diamètre, le barème grand/petit 10, le centre avec/sans X ou les zones visibles. Ces différences deviennent des paramètres dans l’écran qui suit le choix du modèle. Pour la campagne, les dispositions simple, verticale, triangulaire, quatre et douze blasons sont proposées comme paramètres.

La séance enregistre une copie complète de ces paramètres. La rotation du téléphone ne les réinitialise pas. Les anciennes définitions restent conservées pour les historiques et sauvegardes, mais ne surchargent plus le sélecteur standard. Les cibles personnalisées et importées restent accessibles.

Les palettes proposées sont Sport, Forêt, Prune, Graphite et Clair coloré, plus le choix automatique Système. Les couleurs des boutons de score restent celles des zones de cible pour conserver leur sens dans chaque thème. L’ancien réglage Sombre est interprété comme Sport.

Pour mettre à jour depuis Mire 1.1, ouvrir le nouveau dossier et réinstaller avec Android Studio, sans désinstaller l’application. Les scores et coordonnées des anciennes séances restent conservés. Les anciennes coordonnées Mire 1.1 sur silhouette sont transformées uniquement à l’affichage vers le repère neutre ; les sources ne sont pas réécrites. Certains anciens impacts de corps peuvent se situer hors du nouveau contour : le score historique reste la référence. Voir `docs/NOUVEAUTES-1.2.md` et `docs/VALIDATION.md`.


## Mire 1.4.3 — identité et lancement

Nouvelle icône PNG sur fond transparent intégrée aux icônes adaptatives Android et à l’écran de lancement système. L’écran de lancement de l’application reprend le même symbole sur fond bleu nuit avec une animation courte de 850 ms : apparition et mise à l’échelle de l’icône, tracé d’un cercle et apparition du titre. Une erreur de chargement affiche immédiatement le message et le bouton Réessayer. L’animation ne bloque pas l’ouverture de la base et ne se rejoue pas après une simple rotation une fois terminée.

La loupe suit maintenant les deux axes, toujours au-dessus de la croix, avec un décalage continu. La croix reste décalée de 36 dp au-dessus du doigt et bloquée aux bords ; aperçu et enregistrement partagent cette position. La loupe se bloque horizontalement de façon continue, et un espace réservé lui permet de déborder au-dessus du canevas sans masquer les autres commandes.

Voir `docs/NOUVEAUTES-1.3.md` et `docs/IDENTITE-VISUELLE.md`.


## Mire 1.4.3 — présentation et lancement

La cible remonte immédiatement sous les commandes de la volée : aucun espace n’est réservé à la loupe et aucune phrase explicative n’occupe cette zone. La loupe suit le doigt sur les deux axes au-dessus de la croix, dans une superposition non focalisable. Son rayon s’adapte continûment à la place disponible sous la barre système. La croix, le score et l’impact enregistré partagent toujours la même position.

L’icône est remplacée par une illustration mate aux couleurs blanc/noir/bleu/rouge/jaune, avec flèche et carnet de marque. Les calques du lanceur respectent une zone de sécurité pour les masques circulaires. Le PNG source est réellement transparent.

La fenêtre initiale imposée par Android reçoit un fond bleu nuit uni et une icône transparente. Sa sortie est immédiate dès le premier écran de l’app, sans seconde animation système. Le seul écran de marque visible dans l’app dure 2,3 secondes, avec « Ouverture de votre carnet… », puis un fondu de 200 ms vers l’accueil. Si la base charge encore, cet écran reste affiché ; une erreur donne accès au bouton Réessayer. Les animations désactivées au niveau système peuvent raccourcir l’introduction. Aucun pourcentage ni étape de travail fictive n’est annoncé.

Les mentions de technologies et les commentaires de réalisation ont été retirés de l’interface courante. Les libellés et informations utiles aux réglages, imports et résultats sont conservés. Voir `docs/NOUVEAUTES-1.4.md`, `docs/IDENTITE-VISUELLE.md` et `docs/VALIDATION.md`.


## Supprimer ou rouvrir une séance

Dans l’accueil ou l’onglet Séances, chaque carte affiche une corbeille rouge. La toucher ouvre une confirmation avec le nom de la séance ; Annuler conserve toutes les données. Supprimer retire la séance, ses impacts, son journal et ses repères de parcours. Le matériel, les autres séances et les objectifs restent conservés.

Le bouton Rouvrir d’une séance terminée conserve ses flèches, repasse la séance en cours, puis ouvre la saisie après enregistrement de ce changement. Une séance en cours affiche Reprendre. Toucher le reste de la carte garde le comportement habituel : résumé pour une séance terminée, saisie pour une séance en cours.

Le résumé propose également Supprimer dans ses actions en haut de page, avec la même confirmation.


## Mire 1.4.3 — icônes d’action

Reprendre utilise l’icône Material Play Arrow ; Rouvrir utilise Material Restore ; Supprimer utilise Material Delete Outline. Les cartes et le résumé partagent les mêmes icônes vectorielles, teintées selon le thème. Les SVG Google d’origine et leur licence sont conservés dans `artwork/material-icons/`. Les tracés sont repris sans modification dans les ressources vectorielles Android.


## Mire 1.4.3 — fermeture au lancement de l’APK signée

Le journal du Galaxy S21 situe le crash dans AndroidX Startup, lors de ProcessLifecycleInitializer.dependencies, avec IllegalAccessError sur une classe kotlin.collections renommée dans l’APK. L’application ferme avant la création de MainActivity. L’optimisation partielle R8 ciblant AndroidX/Kotlin/Kotlinx est la cause suspectée.

La configuration release désactive maintenant R8 avec optimization.enable = false et retire packageScope. Le package, la signature à réutiliser et les données restent inchangés. Le versionCode passe à 9 et la version à 1.4.3. Cette correction de configuration doit être validée en reconstruisant une APK signée et en la lançant sur le S21 ; aucune exécution réussie sur ce téléphone n’est revendiquée ici.

1. Ouvrir le nouveau dossier complet dans Android Studio et synchroniser Gradle.
2. Exécuter Build → Clean Project pour éviter de reprendre un ancien APK dans le dossier de sortie.
3. Générer une APK signée, variante release, avec le même fichier de clé .jks, le même alias et les mêmes mots de passe que l’APK installée.
4. Installer la nouvelle APK comme mise à jour, sans désinstaller Mire. Vérifier qu’il s’agit bien du fichier nouvellement généré.
5. Vérifier l’ouverture, une séance et le redémarrage de l’application. Si un crash persiste, récupérer le nouveau Logcat.

La tentative de build dans l’environnement de préparation est bloquée dès le téléchargement du Gradle Wrapper (Network is unreachable). Lire docs/VALIDATION.md pour distinguer les contrôles des anciennes versions et cette correction.
