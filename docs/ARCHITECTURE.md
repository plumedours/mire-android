# Architecture

## Organisation

- `domain/Models.kt` : modèles métier immuables, définitions historiques conservées pour compatibilité.
- `domain/TargetEngine.kt` : géométrie des spots, transformation des coordonnées, score et analyse.
- `domain/GoalEngine.kt` : évaluation des objectifs sur une période glissante.
- `domain/DataValidator.kt` : invariants de stockage et import.
- `data/Codec.kt` : sérialisation versionnée et export natif.
- `data/BackupImporter.kt` : validation, conversion Volée, fusion et restauration.
- `data/MireDatabase.kt` : Room, écritures différentielles, transactions et état observable.
- `MireViewModel.kt` : mutations métier sérialisées, chargement et erreurs visibles.
- `ui/` : écrans Compose, formulaires, composants et renderer graphique partagé.
- `export/Exports.kt` : PDF Android natif, classeur Office Open XML, CSV et PNG.

L’interface observe un `StateFlow` avec un abonnement lié au cycle de vie. Les accès disque passent par `Dispatchers.IO`, les conversions lourdes par `Dispatchers.Default`. Aucun réseau, serveur, compte ni clé API n’est nécessaire pour l’usage de l’application.

## Données

Room utilise une table `records`, indexée par type et parent, contenant un document JSON versionné par entité. Les données métier ne sont pas regroupées dans une unique ligne de sauvegarde. Les écritures ne remplacent que les enregistrements modifiés ; la validation et l’écriture sont sérialisées par mutex et transaction. Le catalogue initial est persisté lors de la création.

Ce choix facilite la reprise des champs de Volée et les évolutions du format. Les écrans chargent actuellement le carnet en mémoire ; pour des historiques de plusieurs centaines de milliers d’impacts, une future évolution vers des requêtes paginées et des agrégats SQL sera appropriée.

Les grandes archives source Volée sont découpées en lignes de 100 000 caractères afin de ne pas dépasser la taille d’une fenêtre de curseur SQLite. Le JSON d’origine est reconstitué sans modification. Il figure dans les sauvegardes natives.

Les préférences sont stockées avec les autres métadonnées Room ; il n’y a pas de seconde source de vérité DataStore. Une restauration conserve les préférences actuelles de l’utilisateur.

Les modifications du catalogue ne modifient pas les séances passées : une séance contient une copie de sa cible et du diamètre de tube utilisé. Archiver un élément de matériel garde les références historiques.

## Repères géométriques

Un impact positionné contient :

- `spot` : spot visé, de 0 à 2 ;
- `u`, `v` : coordonnées locales autour de son centre, en unités du rayon nominal du blason complet ;
- `legacyX`, `legacyY` : coordonnées source si import Volée ;
- `uncertain` : conversion ancienne à vérifier ;
- `manual` : score saisi ou modifié manuellement.

`u = 0, v = 0` est le centre. `v > 0` correspond au bas. Un blason complet a un rayon de 1 ; le trispot réduit aux zones 6–10 a un rayon visible de 0.5. Le rayon physique du tube, rapporté au rayon nominal du blason, vaut `diamètre_mm / (diamètre_cm × 10)`.

Dessin et score utilisent la même définition de cible. Les coordonnées ne sont pas en pixels. La superposition utilise les coordonnées locales des spots sans enlever les erreurs de visée. Les saisies sans position sont comptées dans les résultats et exclues des analyses géométriques.

L’écart maximal est calculé sur l’enveloppe convexe avec calipers rotatifs ; les tests comparent ce résultat à une recherche exhaustive sur des jeux aléatoires. Les scores géométriques peuvent différer des anciens calculs erronés de Volée ; les scores historiques stockés ne sont jamais recalculés globalement.

## Barèmes

Le catalogue propose 10 zones, petit 10, campagne 6 zones, Worcester 5 zones, 3D 11 et 12, ballon et paille. Les trispots verticaux et triangulaires prennent en charge les blasons 10 zones et petit 10. Le catalogue est modifiable. Il ne prétend pas constituer un moteur complet de contrôle réglementaire de compétition : affectations obligatoires de flèches aux spots, pénalités et départages restent à gérer selon l’évènement.

## Adaptation

Les pages générales ont une largeur de lecture limitée et deviennent accessibles par rail de navigation sur une fenêtre large. L’écran de tir utilise un seuil de largeur et de hauteur disponibles pour passer à deux panneaux ; le mode gaucher inverse leur ordre. Les fenêtres étroites et les paysages courts restent défilables. Les formulaires sont bornés en largeur, défilables et gardent leurs actions visibles.

## Tests et évolution

Les tests JVM ciblent les calculs, la géométrie, les imports et les exports tabulaires. Les tests instrumentés contrôlent la persistance Room, le rejet de mutation invalide et les archives supérieures à une fenêtre de curseur. Ils nécessitent un appareil pour être exécutés.

Toute évolution du schéma Room doit ajouter une migration ; aucun effacement destructif de repli n’est configuré. Toute évolution du JSON doit conserver un lecteur des versions antérieures ou refuser explicitement une version inconnue.

## Évolution 1.1

`AnimalGeometry.kt` contient les zones neutres du vital partagées entre dessin et score schématique, ainsi que `BoardViewport`, testé indépendamment du système de gestes. `TargetPicker.kt` expose le catalogue visuel ; `ScorePad.kt` définit les couleurs des zones et les grands boutons. `TargetBoard.kt` utilise un seul reconnaisseur de gestes, un fond raster mis en cache et une transformation commune au blason et à la loupe.

Les grilles à quatre et douze blasons sont explicites. Les quatre blasons anglais affichent les zones 5–10, les trispots habituels 6–10 ; les grilles campagne gardent le blason complet. Les anneaux campagne utilisent des rayons relatifs 0.1/0.2/0.4/0.6/0.8/1. Les scores historiques enregistrés restent inchangés. Les métriques physiques de groupement restent désactivées pour les schémas du vital ; leurs cartes de chaleur sont uniquement visuelles.


`TargetCatalog.kt` regroupe les variantes historiques en 11 modèles intégrés paramétrables. Le sélecteur propose diamètre, barème, centre, zones visibles et disposition campagne. `SessionForm` conserve la définition complète sérialisée dans `rememberSaveable`, plutôt qu’un seul identifiant de catalogue. Le champ `zoneStyle` distingue les nouveaux repères neutres (`neutral`) des coordonnées Mire 1.1 (`animal-v1`). Le décodage des anciennes définitions sans champ utilise le repère historique pour Nature/3D. Les conversions aller-retour affectent seulement l’affichage et la saisie déplacée, sans mutation des coordonnées et scores stockés. La sauvegarde native est en version 3 ; les versions natives 1 et 2 restent acceptées.
