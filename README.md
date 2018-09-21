

# Note sur l'usage d'IntelliJ.

IntelliJ ne sait pas interpréter ``<testSource>`` dans le POM et lève des erreurs
```
Error:(198, 48) java: lambda expressions are not supported in -source 1.6 (use -source 8 or higher to enable lambda expressions)
```
sur les classes de test pour qui ``<testSource>`` a pourtant été initialisé à ``1.8``.
Pour éviter ces erreurs, aller dans ``Project Structure`` (Ctrl+Alt+Shift+S), puis  dans ``Modules``, puis mettre
``Language level`` à ``8``.

Ces erreurs sont sans conséquence sur le ``mvn install``.
