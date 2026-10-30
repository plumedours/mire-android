<p align="center">
  <img src="app/src/main/res/drawable-nodpi/mire_brand_flat.png" width="120" alt="Icône de Mire">
</p>

# Mire

**Votre carnet de tir à l’arc, une flèche à la fois.**

**Français** · [English](README.en.md)

![Android 8.0 et supérieur](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Version publiée](https://img.shields.io/github/v/release/plumedours/mire-android?include_prereleases&label=version)

**[Télécharger l’APK](https://github.com/plumedours/mire-android/releases)** · [Nouveautés](CHANGELOG.md) · [Signaler un problème](https://github.com/plumedours/mire-android/issues/new/choose)

Mire accompagne vos entraînements et compétitions de tir à l’arc sur Android. Enregistrez vos points, placez vos impacts et retrouvez votre progression dans un carnet qui fonctionne hors ligne, sans compte.

## Fonctionnalités

- **Séances et compétitions** : saisie des points avec des boutons aux couleurs de la cible ou placement des impacts sur le blason.
- **Blasons configurables** : cibles couleurs, trispots verticaux et triangulaires, campagne, Worcester, Beursault, paille, Nature, 3D et ballon.
- **Visée au doigt** : croix décalée et loupe pour positionner les flèches, avec zoom et déplacement de la vue.
- **Résultats par volée** : consultez l’ensemble de la séance ou balayez pour voir chaque volée ; superposez les spots d’un trispot.
- **Progression** : statistiques, cartes de chaleur, objectifs et journal de séance.
- **Matériel** : arcs, flèches et configurations personnelles.
- **Sauvegardes et partage** : import Volée, sauvegarde complète, exports PDF, Excel, CSV et image de résultat.
- **Personnalisation** : interface française et anglaise, plusieurs palettes et mise en page adaptée à la largeur de l’écran.

Les schémas Nature et 3D représentent les zones de points de façon neutre, sans animal particulier ni échelle physique.

## Installer Mire

1. Ouvrez la page **[Releases](https://github.com/plumedours/mire-android/releases)**.
2. Choisissez la version et téléchargez **`Mire.apk`** dans ses fichiers joints, sous **Assets**.
3. Ouvrez l’APK sur votre téléphone et autorisez l’installation depuis votre gestionnaire de fichiers si Android le demande.

**Android 8.0 ou supérieur est requis.** Consultez les notes de la version pour connaître les appareils testés et les éventuelles limites. Les versions marquées **Pre-release** sont des versions de test.

Pour une mise à jour, installez la nouvelle APK par-dessus la précédente. Une sauvegarde depuis les réglages permet de conserver une copie de votre carnet. Les archives **Source code** proposées par GitHub contiennent le projet, pas l’application à installer.

## Récupérer ses données Volée

Dans Volée, exportez une sauvegarde JSON. Dans Mire, ouvrez **Réglages → Importer Volée ou Mire**, choisissez ce fichier puis consultez l’aperçu avant de fusionner vos données.

## Construire le projet

Ouvrez le dossier contenant `settings.gradle.kts` dans Android Studio. Le projet utilise Kotlin, Jetpack Compose et Room ; la configuration courante demande le SDK Android 37 et un JDK 25 pour le daemon Gradle.

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Pour distribuer l’application, générez une **APK release signée** avec **Build → Generate Signed App Bundle or APK → APK**. Conservez votre clé de signature hors du dépôt et réutilisez-la pour les mises à jour.

- [Guide technique et migration](docs/GUIDE-TECHNIQUE.fr.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Vérifications et limites de validation](docs/VALIDATION.md)
- [Recette sur appareil](docs/RECETTE.md)
- **[Créer le dépôt et publier une release](docs/PUBLICATION-GITHUB.fr.md)**

## Signaler un problème

Ouvrez une [issue](https://github.com/plumedours/mire-android/issues/new/choose) avec la version de Mire, le modèle du téléphone, la version Android et les étapes pour reproduire le problème. En cas de fermeture, joignez le bloc d’erreur Logcat en retirant vos informations personnelles.

Les icônes d’action Google Material et leur licence sont disponibles dans [artwork/material-icons](artwork/material-icons/README.md).
