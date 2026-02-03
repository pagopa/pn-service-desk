# Template personalizzato per OpenAPI Generator 7.4.0

## Contesto

Il template `pojo.mustache` presente in questa directory DEVE essere referenziato solo per la generazione dei client di **pn-delivery-push**.

## Problema

Contrariamente a quanto previsto nel README della versione 5.4.0, l'aggiornamento a OpenAPI Generator 7.4.0 (nell'ambito della migrazione a Spring Boot 3) **NON ha risolto** il problema della gestione del tag discriminator.

Il generatore continua a produrre annotazioni `@JsonTypeInfo` e `@JsonSubTypes` su classi che utilizzano `oneOf`/`anyOf` con discriminator, ma i sottotipi generati **non estendono la classe base**, rendendo impossibile la deserializzazione polimorfica con Jackson.

Errore tipico:
```
com.fasterxml.jackson.databind.exc.InvalidTypeIdException: Could not resolve type id 'XXX' as a subtype of [...]:
Class [...] not subtype of [...]
```

## Soluzione

Il template `pojo.mustache` in questa directory e' una copia del template in pn-commons di OpenAPI Generator 7.4.0 con la seguente modifica:
- **Rimozione delle annotazioni `@JsonTypeInfo` e `@JsonSubTypes`** in caso di presenza di un tag discriminator

Questa soluzione replica l'approccio gia' adottato per la versione 5.4.0.

## Configurazione nel pom.xml

Per utilizzare questo template, aggiungere nella configurazione dell'execution:

```xml
<templateDirectory>${project.basedir}/src/main/resources/templates/7.4.0/client</templateDirectory>
```

## Riferimenti

- Issue correlata: gestione discriminator in OpenAPI Generator con `oneOf`/`anyOf`
- Workaround precedente: `src/main/resources/templates/5.4.0/client/README.md`
