Il template pojo.mustache presente in questa folder DEVE essere referenziato solo per la generazione dei client di
pn-delivery-push.

Il template è una copia del template presente sul modulo pn-commons per i client con versione 5.4.0.
L'unica modifica apportata è la rimozione in caso di presenza di un tag discriminator delle annotation @JsonTypeInfo e @JsonSubTypes

Si sceglie di introdurre questo template come soluzione rapida e momentanea, visto che in roadmap è prevista la migrazione a springboot3
che costringerà ad aumentare la versione del plugin di generazione dei client che gestisce in modo corretto il tag discriminator.

---

**AGGIORNAMENTO (2026-02)**: L'aspettativa che l'aggiornamento del plugin risolvesse il problema si e' rivelata **errata**.
Con la migrazione a Spring Boot 3 e OpenAPI Generator 7.4.0, il problema persiste e si e' dovuto replicare lo stesso
workaround anche per la nuova versione. Vedere: `src/main/resources/templates/7.4.0/client/README.md`