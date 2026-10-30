# Publier Mire sur GitHub

[Français](PUBLICATION-GITHUB.fr.md) · [English](PUBLISHING.en.md)

Cette procédure utilise le dépôt **plumedours/mire-android**. Si vous choisissez un autre propriétaire ou nom, remplacez aussi les liens dans les deux README, le changelog et ce guide.

La publication se fait en deux temps : le code source dans le dépôt, puis l’APK signée dans une release. La clé de signature reste sur votre ordinateur.

## 1. Préparer le dossier

1. Décompressez le projet et ouvrez le dossier qui contient `settings.gradle.kts`.
2. Conservez votre clé `.jks` en dehors du projet, avec une copie de sauvegarde.
3. Ouvrez le terminal d’Android Studio à la racine du projet et vérifiez :

```powershell
git --version
```

Le `.gitignore` fourni exclut les caches, sorties de compilation, APK, archives, clés de signature et sauvegardes personnelles. Les APK seront déposées dans Releases.

## 2. Créer le dépôt vide

1. Connectez-vous au compte GitHub **plumedours**.
2. Cliquez sur **+ → New repository**.
3. Repository name : **mire-android**.
4. Description : **Carnet de tir à l’arc Android · Archery journal for Android**.
5. Choisissez **Public** si les utilisateurs doivent accéder librement au code et aux téléchargements ; **Private** limite l’accès aux personnes autorisées au dépôt.
6. Ne cochez pas l’ajout d’un README, d’un `.gitignore` ou d’une licence : le projet contient déjà ses fichiers de présentation.
7. Cliquez sur **Create repository**.

## 3. Envoyer le projet

Dans le terminal d’Android Studio, depuis le dossier de Mire :

```powershell
git init -b main
git add .
git status
git commit -m "Prepare Mire Android repository"
git remote add origin https://github.com/plumedours/mire-android.git
git push -u origin main
```

Relisez la liste affichée par `git status` avant de créer le commit : aucune clé de signature ni donnée personnelle ne doit être incluse. Si Git réclame votre identité, définissez votre nom et l’adresse de commit souhaitée, puis relancez le commit :

```powershell
git config user.name "Votre nom"
git config user.email "Votre adresse de commit GitHub"
```

Si le dossier appartient déjà à un dépôt Git, ne recommencez pas l’initialisation ni l’ajout du remote : vérifiez `git status` et `git remote -v`, puis faites le commit et le push dans le dépôt approprié. La connexion HTTPS peut demander une authentification GitHub via votre navigateur ou le gestionnaire d’identifiants Git.

## 4. Configurer la page du projet

Sur la page GitHub, dans **About → roue dentée**, renseignez la description, le champ **Website** avec `https://github.com/plumedours/mire-android/releases` et, si souhaité, les sujets `android`, `archery`, `kotlin`, `jetpack-compose` et `offline`.

Le `README.md` est affiché automatiquement. Son lien **English** ouvre `README.en.md`. Le badge de version se remplira après la première release publiée. Le lien **Télécharger l’APK** pointe sur la liste des releases, y compris les versions de test.

## 5. Vérifier la version avant publication

La configuration actuelle dans `app/build.gradle.kts` est :

```kotlin
versionCode = 9
versionName = "1.4.3"
```

La release correspondante utilisera le tag **v1.4.3** et le titre **Mire 1.4.3**.

Pour une version suivante, par exemple `1.4.4`, passez `versionCode` à `10` et `versionName` à `1.4.4`. Mettez également à jour le libellé de version actuellement présent dans `app/src/main/java/com/plumedours/mire/ui/SettingsScreen.kt`, ainsi que `CHANGELOG.md`. Un numéro de version Android supérieur et la même clé de signature permettent de proposer la mise à jour aux installations existantes.

La version 1.4.3 contient le correctif du crash release signalé sur Galaxy S21. Son démarrage signé n’a pas été confirmé dans cette conversation. Vérifiez-le sur appareil avant de présenter cette version comme stable. Sinon, publiez-la avec la case **Set as a pre-release**.

## 6. Construire et vérifier l’APK signée

1. Dans Android Studio : **Build → Clean Project**.
2. Choisissez **Build → Generate Signed App Bundle or APK → APK**.
3. Sélectionnez votre clé `.jks` existante, votre alias et la variante **release**.
4. Après la compilation, utilisez **Locate** pour retrouver l’APK nouvellement générée.
5. Installez-la sur le téléphone, ouvrez-la et vérifiez une création/reprise de séance ainsi qu’une sauvegarde.
6. Copiez cette APK validée dans un dossier de publication et renommez la copie **Mire.apk**. Renommer le fichier ne change pas le numéro de version intégré ni sa signature.

L’APK attachée doit bien provenir de ce code et de ce numéro de version. Conservez la même clé et le même alias pour les mises à jour.

## 7. Publier la première release

1. Envoyez le code final avant de créer le tag :

```powershell
git add .
git commit -m "Prepare release 1.4.3"
git push
```

Si aucun fichier n’a changé depuis le premier commit, Git indiquera qu’il n’y a rien à committer : continuez avec la release.

2. Sur GitHub : **Releases → Create a new release** ou **Draft a new release**.
3. Dans **Choose a tag**, saisissez **v1.4.3**, puis créez le tag sur la branche **main**. Le formulaire de release peut créer ce tag : il n’est pas nécessaire de le créer aussi en ligne de commande.
4. Release title : **Mire 1.4.3**.
5. Copiez le texte de [RELEASE-1.4.3.md](../.github/RELEASE-1.4.3.md). Complétez les appareils testés et les limites connues selon vos essais réels.
6. Dans la zone des pièces jointes, déposez **Mire.apk**. Attendez la fin de l’envoi.
7. Cochez **Set as a pre-release** si la validation sur appareil n’est pas terminée. Pour une version stable validée, utilisez **Set as the latest release** si cette option est proposée.
8. Vous pouvez choisir **Save draft** pour relire la page. Cliquez sur **Publish release** quand le contenu et le fichier sont prêts.

GitHub ajoute ses propres archives **Source code (zip)** et **Source code (tar.gz)**. Elles ne remplacent pas l’APK. Vérifiez que **Mire.apk** figure bien dans les fichiers de la release.

## 8. Liens de téléchargement

Toutes les versions, y compris les préversions :

https://github.com/plumedours/mire-android/releases

Page de cette version après publication :

https://github.com/plumedours/mire-android/releases/tag/v1.4.3

Dernière version stable, une fois qu’il en existe une :

https://github.com/plumedours/mire-android/releases/latest

Téléchargement direct de l’APK de la dernière version stable, à condition de conserver le nom de pièce jointe **Mire.apk** à chaque publication :

https://github.com/plumedours/mire-android/releases/latest/download/Mire.apk

Pour une version de test précise :

https://github.com/plumedours/mire-android/releases/download/v1.4.3/Mire.apk

Les liens `/latest` ne sont pas adaptés lorsqu’il n’existe encore que des préversions. C’est pourquoi les README pointent sur `/releases`. Un dépôt privé demande aux lecteurs un accès autorisé.

## 9. Versions suivantes

1. Modifiez et testez l’application.
2. Augmentez `versionCode` et `versionName`, puis le libellé dans les réglages.
3. Complétez le changelog, commitez et poussez le code.
4. Générez l’APK signée avec la même clé, testez-la, puis renommez la copie **Mire.apk**.
5. Créez une nouvelle release avec un nouveau tag correspondant, par exemple **v1.4.4**.
6. Joignez cette nouvelle APK et publiez la release. Gardez les anciennes releases accessibles.

Le dossier `.github` contient aussi un modèle de notes pour les prochaines versions et un formulaire bilingue de signalement de problème. Aucune automatisation de signature n’est installée : cette procédure ne transmet pas votre clé de signature à GitHub.

## Documentation officielle

- [Envoyer un projet local vers GitHub](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github)
- [Créer et gérer les releases](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)
- [Liens vers les releases](https://docs.github.com/en/repositories/releasing-projects-on-github/linking-to-releases)
