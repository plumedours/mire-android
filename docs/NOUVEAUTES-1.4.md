# Mire 1.4

- Suppression de la zone vide de 136 dp et de sa phrase explicative. La cible commence directement sous les commandes de tir.
- Loupe en superposition pendant la visée, au-dessus de la croix ; suivi horizontal et vertical continu. La limite horizontale et l’adaptation progressive du rayon permettent de rester visible sous les barres système. Elle n’occupe aucune place dans la mise en page et ne prend pas le focus tactile.
- Suppression des mentions Kotlin/Compose et des commentaires techniques dans l’interface courante ; simplification des écrans de paramètres, de saisie et de résultats.
- Nouvelle icône originale, mate, aux couleurs d’un blason de tir : blanc, noir, bleu, rouge et jaune, avec flèche et carnet de marque. PNG RGBA transparent intégré sans retouche. Les marges du lanceur sont augmentées pour protéger le dessin des masques adaptatifs.
- Fenêtre initiale Android sur fond uni avec icône transparente ; suppression de l’animation de sortie système pour éviter l’apparition d’un premier logo rond recadré.
- Écran de marque animé de 2,3 secondes, puis fondu de 200 ms vers l’accueil. Texte « Ouverture de votre carnet… » et indicateur indéterminé. Ce texte n’annonce aucune opération fictive ni pourcentage de chargement.
- Si la base charge plus longtemps, le même écran reste affiché. Les erreurs restent accessibles et l’introduction terminée ne se rejoue pas après une rotation. Les réglages système de réduction des animations restent respectés.

La fenêtre de lancement Android ne peut pas être entièrement supprimée par l’application ; elle est rendue neutre pour assurer une continuité visuelle. Les animations d’ouverture du lanceur peuvent varier selon l’appareil. Le résultat réel, le cadrage des icônes et les gestes doivent être vérifiés sur appareil ; consulter VALIDATION.md.

Aucune modification des formats de sauvegarde ou des anciens résultats. Réinstaller sans désinstaller Mire pour conserver le carnet.
