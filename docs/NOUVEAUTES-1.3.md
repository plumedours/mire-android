# Mire 1.3

## Loupe et visée

La loupe suit maintenant le déplacement sur les deux axes, au-dessus de la croix, sans basculement en dessous et sans changement brusque de côté. Son centre est calculé par un décalage vertical continu ; sa limite horizontale est appliquée continûment. La croix garde le décalage de 36 dp au-dessus du doigt et une marge de 12 dp aux bords. Une zone de 136 dp est réservée au-dessus du canevas pour la loupe lorsque la visée atteint le bord haut. Les coordonnées visées et enregistrées restent identiques, y compris avec le zoom.

## Icône et lancement

Icône PNG originale avec canal alpha, intégrée aux icônes adaptatives, à la variante monochrome Android et à l’écran de lancement système. Le symbole généré est conservé sans retouche dans `app/src/main/res/drawable-nodpi/mire_brand.png`.

Écran d’entrée bleu nuit avec animation de 850 ms : apparition et légère mise à l’échelle du symbole, tracé d’un cercle et entrée du titre MIRE. L’animation s’exécute pendant le chargement et se termine indépendamment de celui-ci. Si la base charge encore après cette animation, le même écran affiche un indicateur de progression ; une erreur interrompt l’écran d’entrée et affiche la possibilité de réessayer. L’accueil est affiché dès que l’animation est terminée et les données sont disponibles. Aucune animation ne retient l’écran de lancement système.

Aucun format de sauvegarde ni historique n’est modifié. Réinstaller sans désinstaller Mire pour conserver le carnet. Les gestes et le rendu Android doivent encore être vérifiés sur appareil ; consulter VALIDATION.md.
