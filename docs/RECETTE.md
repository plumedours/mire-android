# Recette sur appareil

Les cases ci-dessous sont à exécuter dans Android Studio ou sur téléphone/tablette. Elles ne sont pas présentées comme déjà validées.

## Démarrage et affichage

- [ ] Synchronisation Gradle du projet complet, compilation debug, génération Room/KSP.
- [ ] Installation à côté de Volée, nom Mire et nouvelle icône.
- [ ] Démarrage à froid et reprise après passage en arrière-plan.
- [ ] Téléphone étroit, téléphone large, tablette, paysage court et écran partagé.
- [ ] Police système agrandie, thèmes clair/sombre et français/anglais.
- [ ] Mode gaucher sur une fenêtre assez large pour deux panneaux.

## Tir

- [ ] Créer une séance sans matériel, puis avec arc/flèches/configuration.
- [ ] Placer et déplacer un impact, utiliser la loupe et le zoom, annuler.
- [ ] Flèche sur chaque frontière d’anneau, puis avec diamètre de tube renseigné.
- [ ] Saisie de score sans position ; vérifier qu’elle n’invente aucun point.
- [ ] Volée complète et incomplète, suivante, retour à une ancienne volée.
- [ ] Fermer l’application pendant la séance puis la relancer : impacts et numéro de volée conservés.
- [ ] Terminer, ouvrir le résumé et rouvrir.

## Cibles et résultats

- [ ] Tir au centre des trois spots d’un trispot vertical : groupement superposé centré.
- [ ] Trispot triangulaire, filtre d’un spot, vue planche complète et carte de chaleur.
- [ ] Balayage de la séance complète vers chaque volée ; totaux concordants.
- [ ] Blason réduit, petit 10, campagne, Worcester et ballon de diamètre personnalisé.
- [ ] 3D : saisie par zone, absence d’analyse circulaire trompeuse.
- [ ] Parcours de plusieurs cibles sur deux tours, repères et distances réutilisés.
- [ ] Journal, modèle de séance, objectif et filtres de progression.

## Migration et fichiers

- [ ] Sauvegarde JSON réelle de Volée : comparer quantités, totaux et X avant/après.
- [ ] Vérifier un trispot ancien ; replacer un impact ambigu et vérifier son analyse.
- [ ] Même sauvegarde importée deux fois : fusion refusée au second essai.
- [ ] Fichier JSON invalide : aucune modification du carnet.
- [ ] Sauvegarde Mire, restauration après ajout de données, contrôle des totaux.
- [ ] Exporter PDF Lettre, Excel et CSV ; ouvrir avec des lecteurs externes.
- [ ] Partager le PNG et enregistrer le PNG dans un dossier choisi.
- [ ] Annuler le sélecteur d’import ou d’export, sans message d’erreur trompeur.

## Avant diffusion

- [ ] `testDebugUnitTest` et `connectedDebugAndroidTest` passent.
- [ ] Générer et installer une APK signée ; tester le démarrage de la version release.
- [ ] Conserver la clé de signature et une sauvegarde JSON en dehors du dépôt.


## Mire 1.2

- Vérifier que le catalogue standard ne présente qu’un modèle 3D et un modèle Nature, sans nom d’espèce ni silhouette. Vérifier aussi les onze modèles et les cibles personnalisées.
- Configurer un blason 122 cm / petit 10 / zones 5–10 ; tourner l’écran avant de démarrer, puis vérifier ces paramètres dans la séance et après restauration d’une sauvegarde.
- Choisir campagne, puis vérifier les dispositions 1/3/4/12 spots et le triangle. Vérifier l’aperçu et le nombre de spots dans la saisie.
- Vérifier les zones 3D 11/12/10+, 10, 8, 5 et manqué ainsi que les deux ordres de flèches Nature. La saisie par boutons permet de corriger la zone.
- Vérifier que le ballon est un petit disque uni sur fond contrasté, 5 cm par défaut. Tester touché/manqué, glisser-relâcher et les cordons avec un tube renseigné.
- Ouvrir une ancienne séance Nature/3D : scores inchangés, affichage neutre, sauvegarde/restauration sans mutation des coordonnées stockées.
- Parcourir Sport, Forêt, Prune, Graphite, Clair coloré et Système ; vérifier la lisibilité des boutons, menus, accueil et barres système sur téléphone étroit et tablette.


## Mire 1.2.1 — visée au doigt

- Glisser du bas vers le haut puis inversement : la loupe garde sa hauteur en haut du canevas et ne bascule plus sous le doigt. Elle suit horizontalement et reste bornée aux côtés.
- Vérifier la croix au-dessus du doigt (36 dp), son blocage aux quatre bords (marge 12 dp) et l’absence de saut à la ligne centrale.
- Relâcher : l’impact doit apparaître à l’emplacement exact de la croix, avec le score prévisualisé. Répéter avec le zoom et le déplacement de vue, puis en déplaçant une ancienne flèche.
- Déborder du canevas en glissant : la croix reste au bord, puis une seule flèche est enregistrée au relâchement. Un geste annulé ou un zoom à deux doigts ne doit enregistrer aucune flèche.


## Mire 1.3 — suivi de la loupe et démarrage

- Déplacer le doigt en diagonale et verticalement : la loupe doit suivre les deux axes, au-dessus de la croix, et ne pas rester sur une ligne fixe.
- Glisser lentement près de chaque bord et de la ligne médiane : aucun basculement ni saut de côté. Au bord haut, la loupe utilise la zone réservée au-dessus du canevas. Confirmer que le doigt ne masque ni la croix ni la loupe.
- Vérifier la correspondance entre croix, image agrandie, score aperçu et impact ajouté/déplacé, avec et sans zoom.
- Vérifier l’icône dans le lanceur avec un masque rond puis carré arrondi, et avec les icônes monochromes si disponibles.
- Démarrer à froid sur Android 8–11 et Android 12 ou supérieur : même identité au lancement système puis dans l’application, courte animation et accès à l’accueil. Tourner l’écran après le lancement : pas de répétition de l’animation. Avec animations système désactivées, l’accueil doit rester accessible.
- Vérifier le chargement lent et les erreurs de base : indicateur ou message Réessayer, sans attente infinie due à une condition d’animation.


## Mire 1.4

- Ouvrir une séance : la cible doit commencer immédiatement sous les commandes, sans zone vide ni phrase sur la loupe. Vérifier les différentes largeurs et dispositions de blasons.
- Poser puis déplacer le doigt : la loupe apparaît au-dessus de la croix sans modifier la mise en page. Glisser verticalement, horizontalement et en diagonale jusqu’aux bords ; vérifier qu’elle reste visible, au-dessus du doigt et sous les barres système, sans basculement ni saut.
- Vérifier qu’un zoom à deux doigts n’ajoute aucune flèche, qu’un relâchement ajoute une seule flèche, et que sa position correspond à la croix et au score aperçu.
- Démarrer à froid : pas de premier logo rond coupé ; fond uni puis écran de marque animé pendant environ 2,3 secondes, libellé Ouverture de votre carnet…, puis fondu vers l’accueil.
- Vérifier une ouverture lente et une erreur de base. Vérifier la rotation après l’entrée et les animations système désactivées.
- Vérifier les masques rond et carré arrondi du lanceur ainsi que les icônes monochromes. L’illustration doit rester intégralement visible.
- Parcourir les écrans : absence de références aux technologies de développement, de commentaire sur le moteur ou d’indication de réalisation.


## Mire 1.4.1 — actions de séance

- Sur l’accueil et dans Séances, vérifier les corbeilles sur les séances terminées et en cours, à petite et grande largeur.
- Toucher une corbeille : confirmation avec le bon nom, sans ouvrir la séance. Annuler puis vérifier que rien n’a disparu.
- Confirmer sur une séance de test avec impacts, journal et parcours : carte retirée, données associées retirées, autres séances et matériel conservés. Relancer l’app et vérifier la persistance.
- Depuis une liste filtrée sur les séances terminées, Rouvrir doit afficher la saisie, conserver les flèches et le score, puis rendre la séance visible parmi les séances en cours au retour dans la liste.
- Vérifier Reprendre sur les séances en cours et l’ouverture habituelle au toucher du reste de la carte.
- Vérifier Supprimer dans les actions en haut du résumé et la même confirmation.


## Mire 1.4.2 — icônes Material

Vérifier Reprendre avec un triangle de lecture, Rouvrir avec Restore et la corbeille Material sur les cartes et dans le résumé. Vérifier leur lisibilité en thème clair et sombre, ainsi que la confirmation de suppression et la conservation des libellés d’accessibilité. Les actions doivent conserver leur comportement de la version 1.4.1.


## Mire 1.4.3 — validation du crash release sur Galaxy S21

- Ouvrir le projet complet corrigé et vérifier la variante release avec optimisation désactivée.
- Nettoyer puis générer une nouvelle APK release signée avec la même clé et le même alias que la version installée.
- Installer en mise à jour sans désinstallation ; vérifier la version 1.4.3.
- Lancer à froid plusieurs fois sur le S21 et vérifier dans Logcat l’absence du IllegalAccessError de ProcessLifecycleInitializer.
- Créer ou reprendre une séance, fermer l’app puis la rouvrir pour contrôler la persistance. Vérifier aussi l’accès aux réglages et l’export d’une sauvegarde.
- En cas de fermeture persistante, relever le nouveau bloc FATAL EXCEPTION complet et conserver l’APK exacte ayant servi à l’essai.
