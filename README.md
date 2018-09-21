

# Note sur l'usage d'IntelliJ.

IntelliJ ne sait pas interpr�ter ``<testSource>`` dans le POM et l�ve des erreurs
```
Error:(198, 48) java: lambda expressions are not supported in -source 1.6 (use -source 8 or higher to enable lambda expressions)
```
sur les classes de test pour qui ``<testSource>`` a pourtant �t� initialis� � ``1.8``.
Pour �viter ces erreurs, aller dans ``Project Structure`` (Ctrl+Alt+Shift+S), puis  dans ``Modules``, puis mettre
``Language level`` � ``8``.

Ces erreurs sont sans cons�quence sur le ``mvn install``.
