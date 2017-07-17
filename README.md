## Céard í Loinnir?
Is gréasán sóisialta í Loinnir. Is í cuspóir a h-aipe ná pobal agus láthair Ghaelach teicneolaíochta a chur chun cinn agus é a dhéanamh níos éasca nascadh agus bualadh le daoine eile trí mheáin sóisialta leis an dearcadh céanna don Ghaeilge. Tá an leagan amach éasca - nascadh le daoine nua trí rúiléid, agus seomra cainte poiblí i do cheantar áitiúil a chruthú atá bunaithe ar cheantar an ghléis mhóibíligh.

Ba chóir d'Éirinn iomlán a bheith ina Gaeltacht - ní chóir a bheith teanntaithe dá bharr easpa phobail leis an dearcadh céanna. Cruthoimis Gaeltacht dhigiteach. :stuck_out_tongue_winking_eye:

## Súiteáil
Más rud é go dteastaíonn uait an leagan is déanaí a íoslódáil agus d'obair fhéin a chur leis; déan clónáil trí Git.

### An Freastalaí:
Baintear úsáid as Python 3.6, Flask agus PyMongo chun an chúil a chur ag obair sa tslí is bunúsaí. Cuir an freastalaí ar siúl trí ```python3 Loinnir.py (dev|prod))``` a bheidh le fáil ar localhost:3000 nó díreach ar localhost más an leagan táirgeachta (prod) atá i gceist. Caifear ásc de MongoDB a bheith curtha ar siúl sa chúlra leis an bport réamhshocraithe 27017 ionas go mbeidh an chúil in ann míreanna a chur leis an mbunachar sonraí.

Súiteáil aon leabharlanna Python nach bhfuil ann le h-úsáid a bhaint as ```python3 install -r requirements.txt```. Bain usáid as ```pipreqs``` chun leagan nua de requirements.txt a ghineadh i ndiaidh leabharlanna breise a chur leis.

### Críochphointí don API
Is é an leagan amach do na críochphointí ná seo a leanas do léibhinn móibíleacha:
  - /api/v1/users/create
  - /api/v1/users/edit
  - /api/v1/users/delete
  - /api/v1/users/update-location
  - /api/v1/users/get-nearby-count
  - /api/v1/users/get
  - /api/v1/users/get-others
  - /api/v1/users/get-random
  - /api/v1/users/get-matched-count
  - /api/v1/users/get-unmatched-count
  - /api/v1/users/get-blocked-users
  - /api/v1/users/block-user
  - /api/v1/users/unblock-user

  - /api/v1/messages/send-partner-message
  - /api/v1/messages/send-locality-message
  - /api/v1/messages/get-past-conversation-previews
  - /api/v1/messages/get-partner-message-count
  - /api/v1/messages/get-partner-messages
  - /api/v1/messages/get-paginated-partner-messages
  - /api/v1/messages/get-locality-messages
  - /api/v1/messages/get-paginated-locality-messages
  - /api/v1/messages/mark-seen
  - /api/v1/messages/subscribe-partner

### Android:
Oscail an togra le Android Studio agus déan sioncronú le Gradle.

### iOS:
<easpa oibre faoi láthair>

## Rialacha as Cód a Chur leis
Ná dearmad brainse a chruthú ó ```master``` as gné, agus í a chumasc do ```master``` nuair bhíonn an gné críochnaithe. Ainmnigh an bhrainse trí ```<bug|refactor|feature>/<name>```. Cruthófar eisiúint nua le cur amach nuair a thagann an t-am.
