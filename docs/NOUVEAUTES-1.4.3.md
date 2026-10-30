# Mire 1.4.3 — correctif de démarrage release

## Diagnostic

Le Logcat reçu le 16 octobre 2026 montre une exception Unable to get provider androidx.startup.InitializationProvider, causée par IllegalAccessError dans ProcessLifecycleInitializer.dependencies. Une sous-classe ne peut pas accéder à une classe kotlin.collections renommée. Le crash survient avant MainActivity et avant l’ouverture de l’interface.

Le journal confirme l’erreur d’accès dans une APK obfusquée. La configuration release utilisait l’optimisation R8 limitée aux bibliothèques AndroidX, Kotlin et Kotlinx. L’attribution de l’erreur à cette transformation est une inférence à confirmer en comparant le nouveau build sur le même appareil.

## Correction

- Désactivation de l’optimisation release avec optimization.enable = false.
- Retrait de la sélection partielle packageScope.
- Version 1.4.3, versionCode 9, permettant une mise à jour de l’APK 1.4.2 si elle est signée avec la même clé.
- Aucun changement du moteur de score, des données, du schéma Room ou du processus d’import.

Aucun provider AndroidX n’est retiré et aucune exception de lancement n’est masquée. L’APK est toujours produite par la procédure de signature release. La réactivation des optimisations devra faire l’objet d’une validation spécifique sur appareil.

## Références

- https://developer.android.com/reference/tools/gradle-api/9.4/com/android/build/api/dsl/Optimization : enable contrôle R8 ; false désactive l’optimisation.
- https://developer.android.com/topic/performance/app-optimization/optimize-specified-packages : optimisation partielle expérimentale et nécessité de vérifier la stabilité.

## Validation

Contrôles statiques de la configuration, de la version et de l’archive effectués. Build Gradle tenté après la correction mais bloqué avant compilation par Network is unreachable lors du téléchargement du wrapper. Aucun nouveau résultat de compilation Kotlin, de tests JVM, de génération KSP ou d’installation Android n’est revendiqué pour cette livraison. L’essai de l’APK signée sur le Galaxy S21 est nécessaire pour confirmer la résolution.
