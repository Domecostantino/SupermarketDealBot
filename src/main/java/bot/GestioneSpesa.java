package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

class GestioneSpesa {

    static SendMessage messaggioBenvenuto(Update update) {
	SendMessage mex = new SendMessage().setChatId(update.getMessage().getChatId());
	String testoMex = "Hai appena cliccato sulla funzionalità principale del bot!\n\n"
		+ "Userò la tua lista della spesa per consigliarti in quale supermarket"
		+ "recarti per usufruire delle migliori offerte!\nSe la tua lista non è ancora completa"
		+ " usa il comando /aggiungi per inserire altri prodotti o /lista per visionarla e modificarla. "
		+ "\nQuella che segue è una procedura guidata quindi ti prego di non inviarmi _altri comandi_ "
		+ "(che iniziano con /) mentre è in corso la spesa."
		+ "\n\nQuando sei pronto indicami il raggio massimo da considerare (_distanza dei supermarkets_) "
		+ "rispetto alla tua attuale posizione, per cercare le offerte."
		+ " Clicca su una delle scelte possibili.";
	mex.setText(testoMex);
	mex.setParseMode("Markdown");

	// inseriamo la Custom Keyboard con i raggi possibili
	ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
	List<KeyboardRow> keyboard = new ArrayList<>();
	KeyboardRow row = new KeyboardRow();
	row.add("Minima: 5 Km");
	keyboard.add(row);
	row = new KeyboardRow();
	row.add("Media: 10 Km");
	keyboard.add(row);
	row = new KeyboardRow();
	row.add("Massima: 20 Km");
	keyboard.add(row);
	keyboardMarkup.setKeyboard(keyboard);
	keyboardMarkup.setResizeKeyboard(true);
	mex.setReplyMarkup(keyboardMarkup);
	return mex;
    }

    static boolean ricevutoRaggioMassimo(String testoMex) {
	return testoMex.equals("Minima: 5 Km") || testoMex.equals("Media: 10 Km") || testoMex.equals("Massima: 20 Km");
    }

    static boolean callbackGestioneSpesa(String call_data) {
	return call_data.equals("Ok, procediamo") || call_data.equals("Offerte sul volantino")
		|| call_data.equals("Indietro ◀") || call_data.equals("Sono al supermarket!")
		|| ricevutoSupermarket(call_data);
    }

    private static boolean ricevutoSupermarket(String testoMex) {
	if (testoMex.equals("🔙   Annulla, fammi ricominciare  🔙")) {
	    return true;
	}
	List<Supermarket> supermarkets = GestioneDB.listaSupermercati();
	for (Supermarket sm : supermarkets) {
	    if (testoMex.equals(sm.getNome() + ", " + sm.getIndirizzo()))
		return true;
	}
	return false;
    }

    static boolean interazioneUtente(String testoMex) {
	return ricevutoRaggioMassimo(testoMex) || testoMex.equals("📄  Offerte su tutti i volantini  📄")
		|| testoMex.equals("Ripristina lista iniziale") || testoMex.equals("Prossimo supermercato 🔜")
		|| testoMex.equals("Sì ✔") || testoMex.equals("No ✖") || testoMex.equals("🔝  Spesa Finita  🔝")
		|| testoMex.equals("Vai a quello dopo ancora") || ricevutoSupermarket(testoMex);
    }

    static SendMessage gestisciInterazione(Update update, Map<Long, InfoUtente> scelteUtenti, SupermarketDealBot smdb) {
	long chat_id = update.getMessage().getChatId();
	SendMessage mex = new SendMessage().setChatId(chat_id);
	String testoRicevuto = update.getMessage().getText();
	String testoMex = "";

	// inviato raggio desidarato
	if (ricevutoRaggioMassimo(testoRicevuto)) {
	    // creaiamo l'oggetto UtenteInfo relativo al mittente, che sarà
	    // fondamentale in seguito
	    InfoUtente informazioniUtenteCorrente = new InfoUtente();
	    scelteUtenti.put(chat_id, informazioniUtenteCorrente);
	    // aggiungiamo a infoutente la sua lista di prodotti
	    informazioniUtenteCorrente.setListaUtenteIniziale(smdb.getlistaSpesaUtente(chat_id));

	    switch (testoRicevuto) {
	    case "Minima: 5 Km": {
		informazioniUtenteCorrente.setRaggioMassimo(5);
		break;
	    }
	    case "Media: 10 Km": {
		informazioniUtenteCorrente.setRaggioMassimo(10);
		break;
	    }
	    case "Massima: 20 Km": {
		informazioniUtenteCorrente.setRaggioMassimo(20);
		break;
	    }
	    }

	    testoMex = "Bene hai scelto una distanza massima di " + informazioniUtenteCorrente.getRaggioMassimo()
		    + " Km.\n\n" + "Ora per favore inviami la tua *posizione* cliccando sul bottone qui sotto"
		    + " (occorrono un paio di secondi per elaborare la richiesta):";
	    mex.setParseMode("Markdown");

	    // inseriamo la nuova Custom Keyboard
	    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
	    List<KeyboardRow> keyboard = new ArrayList<>();
	    KeyboardRow row = new KeyboardRow();
	    KeyboardButton button = new KeyboardButton("Invia Posizione").setRequestLocation(true);
	    row.add(button);
	    keyboard.add(row);
	    keyboardMarkup.setKeyboard(keyboard);
	    keyboardMarkup.setResizeKeyboard(true);
	    mex.setReplyMarkup(keyboardMarkup);

	}

	// premuto bottone sulla custom Keyboard mentre si sta facendo la spesa,
	// gestiamo i tre casi

	else if (testoRicevuto.equals("📄  Offerte su tutti i volantini  📄")) {
	    testoMex = "Scegli il supermercato del quale vuoi vedere le offerte oppure "
		    + "_Indietro_ per ritornare alla tua lista e continuare la spesa.";
	    mex.setParseMode("Markdown");
	    mex.setText(testoMex);
	    // aggiungiamo la inline keyboard con la lista dei volantini e il
	    // comando per tornare indietro
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> listaComandi = new ArrayList<>();
	    List<Supermarket> supermarketsVicini = calcolaSupermarketsVicini(
		    scelteUtenti.get(chat_id).getRaggioMassimo(), scelteUtenti.get(chat_id).getPosizione());

	    for (Supermarket sm : supermarketsVicini) {
		String testo = sm.getNome() + ", " + sm.getIndirizzo();
		List<InlineKeyboardButton> riga = new ArrayList<>();
		riga.add(new InlineKeyboardButton().setText(testo).setCallbackData(testo));
		listaComandi.add(riga);
	    }
	    String testo = "Indietro ◀";
	    List<InlineKeyboardButton> riga = new ArrayList<>();
	    riga.add(new InlineKeyboardButton().setText(testo).setCallbackData(testo));
	    listaComandi.add(riga);
	    markupInline.setKeyboard(listaComandi);
	    mex.setReplyMarkup(markupInline);
	}

	else if (testoRicevuto.equals("Ripristina lista iniziale")) {

	    // inseriamo la Custom Keyboard con i 3 bottoni appositi si fa questo passaggio
	    // perchè questo comando può essere richiamato anche con una custom keyboard
	    // diversa
	    aggiungiCustomKeyboardPrincipale3Bott(mex, scelteUtenti.get(chat_id));

	    mex.setText("Ho ripristinato la tua lista iniziale!");
	    smdb.inviaMessaggio(mex);

	    // ora ristampiamo la lista
	    mex = new SendMessage().setChatId(chat_id);
	    LinkedList<Prodotto> nuovaLista = new LinkedList<>();
	    for (String nome : scelteUtenti.get(chat_id).getListaUtenteIniziale()) {
		nuovaLista.add(new Prodotto(nome));
	    }
	    smdb.setlistaSpesaUtente(chat_id, nuovaLista);
	    // creiamo il nuovo messaggio
	    StringBuilder sb = new StringBuilder();
	    String infoSM = scelteUtenti.get(chat_id).getListaSMdaVisitare().get(0);
	    String nome = infoSM.substring(0, infoSM.indexOf(','));
	    String indirizzo = infoSM.substring(infoSM.indexOf(',') + 2);
	    List<Offerta> offerteSupVicino = GestioneDB.estraiOfferteSupermarket(nome, indirizzo,
		    smdb.getlistaSpesaUtente(chat_id));

	    sb.append("Le offerte relative al supermarket *" + nome + "* , in accordo alla tua lista, sono: \n\n");
	    for (Prodotto elementoListaSpesa : smdb.getlistaSpesaUtente(chat_id)) {
		sb.append("\n_" + elementoListaSpesa.getNome() + "_\n\n");
		for (Offerta offerta : offerteSupVicino) {
		    if (offerta.getProdotto().equals(elementoListaSpesa)) {
			sb.append("- " + offerta.getDescrizione() + ", prezzo: "
				+ String.format("%1.2f", offerta.getPrezzo()) + "\n");
		    }
		}
	    }
	    mex.setParseMode("Markdown");
	    smdb.getLista(mex);
	    testoMex = sb.toString() + "\n\n" + mex.getText();
	}

	else if (testoRicevuto.equals("Prossimo supermercato 🔜")) {
	    // verifichiamo che ci siano altri supermercati, in caso negativo,
	    // NON DOVREBBE SERVIRE PIU'
	    if (scelteUtenti.get(chat_id).getListaSMdaVisitare().isEmpty()) {
		testoMex = "Questo era l'ultimo Supermarket della tua lista.\n\nSpero che tu abbia apprezzato l'esperienza. Buona giornata.";
		scelteUtenti.remove(chat_id);
		ReplyKeyboardRemove remCK = new ReplyKeyboardRemove();
		mex.setReplyMarkup(remCK);
		mex.setText(testoMex);
		return mex;
	    }

	    List<Prodotto> listaUtenteCorrente = smdb.getlistaSpesaUtente(chat_id);
	    String prosSM = scelteUtenti.get(chat_id).getListaSMdaVisitare().get(0);
	    String nomeProsSM = prosSM.substring(0, prosSM.indexOf(','));

	    // verficare che la lista non sia vuota
	    if (listaUtenteCorrente.isEmpty()) {
		testoMex = "La tua lista della spesa è vuota! Vuol dire che dovresti aver "
			+ "finito di comperare. Vuoi comunque recarti al prossimo supermaket?";
	    }

	    // verificare che al prossimo supermercato siano presenti offerte nelle
	    // categorie rimaste ed avvisare se si vuole andare
	    else if (!GestioneDB.offertePresentiNelProssimoSM(nomeProsSM, listaUtenteCorrente)) {
		testoMex = "Nel prossimo supermercato non sono presenti offerte relative ai prodotti "
			+ "nella tua lista, vuoi andare comunque?";
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		row.add("Sì ✔");
		row.add("No ✖");
		keyboard.add(row);

		if (scelteUtenti.get(chat_id).getListaSMdaVisitare().size() > 2) {
		    row = new KeyboardRow();
		    row.add("Vai a quello dopo ancora");
		    keyboard.add(row);
		}
		row = new KeyboardRow();
		row.add("🔝  Spesa Finita  🔝");
		keyboard.add(row);
		keyboardMarkup.setKeyboard(keyboard);
		keyboardMarkup.setResizeKeyboard(true);
		mex.setReplyMarkup(keyboardMarkup);

		mex.setText(testoMex);
		return mex;
	    }

	    // ci sono offerte valide nel prossimo supermarket
	    else {
		testoMex = "Vuoi davvero proseguire verso il prossimo Supermaket?";
	    }

	    // ora chiediamo tramite una nuova custom Keyboard con tre pulsanti
	    // inseriamo la Custom Keyboard con i raggi possibili
	    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
	    List<KeyboardRow> keyboard = new ArrayList<>();
	    KeyboardRow row = new KeyboardRow();
	    row.add("Sì ✔");
	    row.add("No ✖");
	    keyboard.add(row);
	    row = new KeyboardRow();
	    row.add("🔝  Spesa Finita  🔝");
	    keyboard.add(row);
	    keyboardMarkup.setKeyboard(keyboard);
	    keyboardMarkup.setResizeKeyboard(true);
	    mex.setReplyMarkup(keyboardMarkup);
	}

	// gestione dei quattro pulsanti della fase finale della spesa

	else if (testoRicevuto.equals("Sì ✔")) {
	    // eliminiamo il vecchio supermarket dalla lista
	    scelteUtenti.get(chat_id).getListaSMdaVisitare().remove(0);

	    mex.setText("Bene, ora ti invierò la posizione del prossimo supermarket che hai scelto. "
		    + "Premici sopra per recarti su _Google Maps_ che ti indicherà il percorso più breve per il prossimo supermarket.\n\n"
		    + "Ricorda di *non premere* altri comandi che iniziano con '/' durante la spesa:");
	    mex.setParseMode("Markdown");
	    ReplyKeyboardRemove remCK = new ReplyKeyboardRemove();
	    mex.setReplyMarkup(remCK);
	    smdb.inviaMessaggio(mex);

	    String nomePrimoSupermarket = scelteUtenti.get(chat_id).getListaSMdaVisitare().get(0);
	    nomePrimoSupermarket = nomePrimoSupermarket.substring(0, nomePrimoSupermarket.indexOf(','));
	    Supermarket primoSupermarket = GestioneDB.posizioneSupermarket(nomePrimoSupermarket);
	    SendLocation location = new SendLocation().setChatId(chat_id).setLatitude(primoSupermarket.getLatitudine())
		    .setLongitude(primoSupermarket.getLongitudine());
	    try {
		smdb.execute(location);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }

	    mex = new SendMessage().setChatId(chat_id);
	    testoMex = "Quando hai raggiunto il supermarket premi sul _bottone_ qui sotto.";
	    mex.setParseMode("Markdown");

	    // aggiungiamo inline con un solo bottone
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> listaComandi = new ArrayList<>();
	    List<InlineKeyboardButton> riga = new ArrayList<>();
	    riga.add(
		    new InlineKeyboardButton().setText("Sono al supermarket!").setCallbackData("Sono al supermarket!"));
	    listaComandi.add(riga);
	    markupInline.setKeyboard(listaComandi);
	    mex.setReplyMarkup(markupInline);
	}

	else if (testoRicevuto.equals("No ✖")) {
	    mex = creaMessaggioSpesaDelSMCorrente(scelteUtenti.get(chat_id), smdb, chat_id, mex);
	    testoMex = mex.getText();
	}

	else if (testoRicevuto.equals("Vai a quello dopo ancora")) {
	    // eliminiamo il vecchio supermarket dalla lista e quello non visitato
	    scelteUtenti.get(chat_id).getListaSMdaVisitare().remove(0);
	    scelteUtenti.get(chat_id).getListaSMdaVisitare().remove(0);

	    mex.setText("Bene, ora ti invierò la posizione del prossimo (saltandone uno) supermarket che hai scelto. "
		    + "Premici sopra per recarti su _Google Maps_ che ti indicherà il percorso più breve per il prossimo supermarket.\n\n"
		    + "Ricorda di *non premere* altri comandi che iniziano con '/' durante la spesa:");
	    mex.setParseMode("Markdown");
	    ReplyKeyboardRemove remCK = new ReplyKeyboardRemove();
	    mex.setReplyMarkup(remCK);
	    smdb.inviaMessaggio(mex);

	    String nomePrimoSupermarket = scelteUtenti.get(chat_id).getListaSMdaVisitare().get(0);
	    nomePrimoSupermarket = nomePrimoSupermarket.substring(0, nomePrimoSupermarket.indexOf(','));
	    Supermarket primoSupermarket = GestioneDB.posizioneSupermarket(nomePrimoSupermarket);
	    SendLocation location = new SendLocation().setChatId(chat_id).setLatitude(primoSupermarket.getLatitudine())
		    .setLongitude(primoSupermarket.getLongitudine());
	    try {
		smdb.execute(location);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }

	    mex = new SendMessage().setChatId(chat_id);
	    testoMex = "Quando hai raggiunto il supermarket premi sul _bottone_ qui sotto.";
	    mex.setParseMode("Markdown");

	    // aggiungiamo inline con un solo bottone
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> listaComandi = new ArrayList<>();
	    List<InlineKeyboardButton> riga = new ArrayList<>();
	    riga.add(
		    new InlineKeyboardButton().setText("Sono al supermarket!").setCallbackData("Sono al supermarket!"));
	    listaComandi.add(riga);
	    markupInline.setKeyboard(listaComandi);
	    mex.setReplyMarkup(markupInline);
	}

	else if (testoRicevuto.equals("🔝  Spesa Finita  🔝")) {
	    if (scelteUtenti.get(chat_id).getListaSMdaVisitare().isEmpty())
		testoMex = "Questo era l'ultimo Supermarket della tua lista.\n\nSpero che tu abbia apprezzato l'esperienza. Buona giornata.";
	    else
		testoMex = "Hai concluso la tua spesa.\n\nSpero che tu abbia apprezzato l'esperienza. Buona giornata.";
	    scelteUtenti.remove(chat_id);
	    ReplyKeyboardRemove remCK = new ReplyKeyboardRemove();
	    mex.setReplyMarkup(remCK);
	    mex.setText(testoMex);
	    scelteUtenti.remove(chat_id);
	}

	// premuto bottone per rieffettuare la scelta dei supermercati
	else if (testoRicevuto.equals("🔙   Annulla, fammi ricominciare  🔙")) {
	    scelteUtenti.get(chat_id).azzeraListaSupermarket();

	    testoMex = "Hai annullato la tua selezioni di supermarkets. Riprova: \n\n";
	    ReplyKeyboardRemove remInlineK = new ReplyKeyboardRemove();
	    mex.setReplyMarkup(remInlineK);
	    mex.setText(testoMex);
	    smdb.inviaMessaggio(mex);

	    mex = new SendMessage().setChatId(update.getMessage().getChatId());
	    elencaSupermarketVicini(mex, scelteUtenti, chat_id);
	    testoMex = mex.getText();

	}

	// abbiamo ricevuto il nome di un supermercato
	else {
	    if (!scelteUtenti.get(chat_id).getListaSMdaVisitare().contains(testoRicevuto)) {
		scelteUtenti.get(chat_id).aggiungiSupermarket(testoRicevuto);
	    }
	    StringBuilder sb = new StringBuilder();
	    sb.append("I supermercati che vuoi visitare sono: \n\n");

	    for (String infoSup : scelteUtenti.get(chat_id).getListaSMdaVisitare()) {
		sb.append(infoSup);
		sb.append("\n\n");
	    }
	    sb.append("Se vuoi dare un'occhiata alle offerte di un supermercato, "
		    + "relative hai prodotto che hai scelto clicca su *Offerte Volantino*.\n"
		    + "Quando hai finito clicca su _Ok procediamo_.");
	    testoMex = sb.toString();

	    // aggiungiamo la inline keyboard con i due messaggi
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> listaComandi = new ArrayList<>();
	    List<InlineKeyboardButton> riga = new ArrayList<>();
	    riga.add(new InlineKeyboardButton().setText("Offerte sul volantino")
		    .setCallbackData("Offerte sul volantino"));
	    listaComandi.add(riga);
	    riga = new ArrayList<>();
	    riga.add(new InlineKeyboardButton().setText("Ok, procediamo 🛒").setCallbackData("Ok, procediamo"));
	    listaComandi.add(riga);

	    markupInline.setKeyboard(listaComandi);
	    mex.setReplyMarkup(markupInline);
	    mex.setParseMode("Markdown");
	}

	mex.setText(testoMex);
	return mex;
    }

    static SendMessage inviataPosizione(Update update, Map<Long, InfoUtente> scelteUtenti) {

	long chat_id = update.getMessage().getChatId();
	SendMessage mex = new SendMessage().setChatId(chat_id);
	Location posizione = update.getMessage().getLocation();

	scelteUtenti.get(chat_id).setPosizione(posizione);

	elencaSupermarketVicini(mex, scelteUtenti, chat_id);
	return mex;
    }

    private static void elencaSupermarketVicini(SendMessage mex, Map<Long, InfoUtente> scelteUtenti, long chat_id) {

	List<Supermarket> smVicini = calcolaSupermarketsVicini(scelteUtenti.get(chat_id).getRaggioMassimo(),
		scelteUtenti.get(chat_id).getPosizione());
	String testoMex = "";

	// decoriamo il messaggio con i sm vicini
	if (smVicini.isEmpty()) {
	    testoMex = "Mi dispiace ma non sono presenti supermercati nel raggio che hai considerato. "
		    + "Puoi selezionare un raggio più ampio premendo su /spesa_ottimizzata oppure "
		    + "usare il bot come lista della spesa interattiva, usando /lista nei negozi "
		    + "vicino a te che non si sono ancora uniti a Supermarket Deal Bot.";
	    ReplyKeyboardRemove remCK = new ReplyKeyboardRemove();
	    mex.setReplyMarkup(remCK);
	} else {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Entro " + scelteUtenti.get(chat_id).getRaggioMassimo()
		    + " Km in linea d'aria sono presenti i seguenti supermarkets:\n\n");
	    for (Supermarket s : smVicini) {
		sb.append("_" + s.getNome() + "_ - " + s.getCitta() + ", " + s.getIndirizzo() + " - _distanza_: "
			+ String.format("%1.3f", s.getDistanzaUt()) + " Km\n\n");
	    }
	    sb.append(
		    "Ora tramite i pulsanti indicami quali supermarket devo considerare (proprio come hai fatto quando hai scelto i prodotti)."
			    + " RICORDA: l'ordine in cui li selezioni è importante, il primo che scegli sarà il _primo che visiterai_,"
			    + " la seconda scelta sarà il secondo in cui ti recherai e così via...Puoi scegliere _quanti supermarket vuoi_, anche solo uno.\n"
			    + "Non aver paura di sbagliare, se vuoi ripetere la scelta basta cliccare sul pulsante *Annulla, fammi ricominciare* in fondo.");

	    // aggiungiamo i bottoni per scegliere i supermercati
	    GestioneSpesa.aggiungiBottoniSupermarkets(mex, smVicini);

	    testoMex = sb.toString();
	    mex.setParseMode("Markdown");
	}
	mex.setText(testoMex);
    }

    private static void aggiungiBottoniSupermarkets(SendMessage mex, List<Supermarket> smVicini) {
	ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
	List<KeyboardRow> keyboard = new ArrayList<>();
	for (Supermarket sm : smVicini) {
	    KeyboardRow row = new KeyboardRow();
	    KeyboardButton button = new KeyboardButton(sm.getNome() + ", " + sm.getIndirizzo());
	    row.add(button);
	    keyboard.add(row);
	}

	// inseriamo il bottone annulla
	KeyboardRow row = new KeyboardRow();
	KeyboardButton button = new KeyboardButton("🔙   Annulla, fammi ricominciare  🔙");
	row.add(button);
	keyboard.add(row);

	keyboardMarkup.setKeyboard(keyboard);
	keyboardMarkup.setResizeKeyboard(true);
	mex.setReplyMarkup(keyboardMarkup);

    }

    private static List<Supermarket> calcolaSupermarketsVicini(int distanzaMassima, Location posizione) {
	float latUtente = posizione.getLatitude();
	float longiUtente = posizione.getLongitude();
	List<Supermarket> tuttiSupermarket = GestioneDB.listaSupermercati();

	// ora recuperiamo i supermercati vicini
	List<Supermarket> vicini = new LinkedList<>();
	for (Supermarket s : tuttiSupermarket) {
	    double distanza = calcolaDistanza(latUtente, longiUtente, s.getLatitudine(), s.getLongitudine());
	    if (distanza < distanzaMassima) {
		Supermarket nuovoSup = new Supermarket(s);
		nuovoSup.setDistanzaUt(distanza);
		vicini.add(nuovoSup);
	    }
	}

	// ordiniamo i supermarket per distanza decrescente
	Collections.sort(vicini, new Comparator<Supermarket>() {

	    @Override
	    public int compare(Supermarket s1, Supermarket s2) {
		if (s1.getDistanzaUt() > s2.getDistanzaUt()) {
		    return 1;
		}
		return -1;
	    }
	});

	return vicini;
    }

    // Calcolo della distanza tra coordinate usando la formula di Haversine
    private static double calcolaDistanza(float lat1, float lon1, float lat2, float lon2) {
	int EARTH_RADIUS = 6371; // Approx Earth radius in KM
	double dLat = Math.toRadians((lat2 - lat1));
	double dLong = Math.toRadians((lon2 - lon1));

	lat1 = (float) Math.toRadians(lat1);
	lat2 = (float) Math.toRadians(lat2);

	double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLong / 2), 2);
	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

	return EARTH_RADIUS * c;
    }

    private static void aggiungiCustomKeyboardPrincipale3Bott(SendMessage mex, InfoUtente infoUt) {
	ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
	List<KeyboardRow> keyboard = new ArrayList<>();
	KeyboardRow row = new KeyboardRow();
	row.add("📄  Offerte su tutti i volantini  📄");
	keyboard.add(row);
	row = new KeyboardRow();
	row.add("Ripristina lista iniziale");
	keyboard.add(row);
	row = new KeyboardRow();
	if (infoUt.getListaSMdaVisitare().size() < 2) {
	    row.add("🔝  Spesa Finita  🔝");
	} else {
	    row.add("Prossimo supermercato 🔜");
	}
	keyboard.add(row);
	keyboardMarkup.setKeyboard(keyboard);
	keyboardMarkup.setResizeKeyboard(true);
	mex.setReplyMarkup(keyboardMarkup);
    }

    static void gestisciCallback(Update update, Map<Long, InfoUtente> scelteUtenti, SupermarketDealBot smdb) {

	String call_data = update.getCallbackQuery().getData();
	long message_id = update.getCallbackQuery().getMessage().getMessageId();
	long chat_id = update.getCallbackQuery().getMessage().getChatId();

	// per risolvere il problema del loading su Android
	AnswerCallbackQuery ack = new AnswerCallbackQuery();
	ack.setCallbackQueryId(update.getCallbackQuery().getId());

	// Ora si gestiscono le varie callbacks della inline keyboard
	if (call_data.equals("Ok, procediamo")) {

	    // diciamo che siamo nella fase finale della spesa, utile per gestire delle
	    // callback successive
	    scelteUtenti.get(chat_id).setFaseFinaleSpesa(true);

	    SendMessage mex = new SendMessage().setChatId(chat_id);
	    mex.setText("Bene, ora ti invierò la posizione del primo supermarket che hai scelto. "
		    + "Premici sopra per recarti su _Google Maps_ che ti indicherà il percorso più breve per il prossimo supermarket.\n\n"
		    + "Ricorda di *non premere* altri comandi che iniziano con '/' durante la spesa:");
	    mex.setParseMode("Markdown");
	    ReplyKeyboardRemove remCK = new ReplyKeyboardRemove();
	    mex.setReplyMarkup(remCK);
	    smdb.inviaMessaggio(mex);

	    String nomePrimoSupermarket = scelteUtenti.get(chat_id).getListaSMdaVisitare().get(0);
	    nomePrimoSupermarket = nomePrimoSupermarket.substring(0, nomePrimoSupermarket.indexOf(','));
	    Supermarket primoSupermarket = GestioneDB.posizioneSupermarket(nomePrimoSupermarket);
	    SendLocation location = new SendLocation().setChatId(chat_id).setLatitude(primoSupermarket.getLatitudine())
		    .setLongitude(primoSupermarket.getLongitudine());
	    try {
		smdb.execute(ack); // per risolvere il problema del loading su Android
		smdb.execute(location);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }

	    mex = new SendMessage().setChatId(chat_id);
	    mex.setText("Quando hai raggiunto il supermarket premi sul _bottone_ qui sotto.");
	    mex.setParseMode("Markdown");

	    // aggiungiamo inline con un solo bottone
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> listaComandi = new ArrayList<>();
	    List<InlineKeyboardButton> riga = new ArrayList<>();
	    riga.add(
		    new InlineKeyboardButton().setText("Sono al supermarket!").setCallbackData("Sono al supermarket!"));
	    listaComandi.add(riga);
	    markupInline.setKeyboard(listaComandi);
	    mex.setReplyMarkup(markupInline);
	    smdb.inviaMessaggio(mex);

	}

	else if (call_data.equals("Offerte sul volantino")) {

	    EditMessageReplyMarkup new_message = new EditMessageReplyMarkup().setChatId(chat_id)
		    .setMessageId((int) message_id);

	    // aggiungiamo la inline keyboard con la lista dei volantini e il
	    // comando per tornare indietro
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> listaComandi = new ArrayList<>();
	    List<Supermarket> supermarketsVicini = calcolaSupermarketsVicini(
		    scelteUtenti.get(chat_id).getRaggioMassimo(), scelteUtenti.get(chat_id).getPosizione());

	    for (Supermarket sm : supermarketsVicini) {
		String testo = sm.getNome() + ", " + sm.getIndirizzo();
		List<InlineKeyboardButton> riga = new ArrayList<>();
		riga.add(new InlineKeyboardButton().setText(testo).setCallbackData(testo));
		listaComandi.add(riga);
	    }
	    String testo = "Indietro ◀";
	    List<InlineKeyboardButton> riga = new ArrayList<>();
	    riga.add(new InlineKeyboardButton().setText(testo).setCallbackData(testo));
	    listaComandi.add(riga);
	    markupInline.setKeyboard(listaComandi);
	    new_message.setReplyMarkup(markupInline);

	    try {
		smdb.execute(ack); // per risolvere il problema del loading su Android
		smdb.execute(new_message);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }
	}

	else if (call_data.equals("Indietro ◀")) {
	    EditMessageText new_message = new EditMessageText().setChatId(chat_id).setMessageId((int) message_id);

	    // siamo nella fase finale della spesa_ottimizzata in cui si è già al
	    // supermarkets, si deve stampare la lista interattiva
	    if (scelteUtenti.get(chat_id).isFaseFinaleSpesa()) {
		SendMessage messaggioTMP = new SendMessage().setChatId(chat_id);
		smdb.getLista(messaggioTMP);
		StringBuilder sb = new StringBuilder();
		String infoSM = scelteUtenti.get(chat_id).getListaSMdaVisitare().get(0);
		String nome = infoSM.substring(0, infoSM.indexOf(','));
		String indirizzo = infoSM.substring(infoSM.indexOf(',') + 2);
		List<Offerta> offerteSupVicino = GestioneDB.estraiOfferteSupermarket(nome, indirizzo,
			smdb.getlistaSpesaUtente(chat_id));

		sb.append("Le offerte relative al supermarket *" + nome + "* , in accordo alla tua lista, sono: \n\n");
		for (Prodotto elementoListaSpesa : smdb.getlistaSpesaUtente(chat_id)) {
		    sb.append("\n_" + elementoListaSpesa.getNome() + "_\n\n");
		    for (Offerta offerta : offerteSupVicino) {
			if (offerta.getProdotto().equals(elementoListaSpesa)) {
			    sb.append("- " + offerta.getDescrizione() + ", prezzo: "
				    + String.format("%1.2f", offerta.getPrezzo()) + "\n");
			}
		    }
		}
		new_message.setParseMode("Markdown");
		new_message.setText(sb.toString() + "\n\n" + messaggioTMP.getText());
		new_message.setReplyMarkup((InlineKeyboardMarkup) messaggioTMP.getReplyMarkup());
	    }

	    // siamo nella fase iniziale della spesa_ottimizzata in cui si scelgono i
	    // supermarkets in cui recarsi
	    else {
		StringBuilder sb = new StringBuilder();
		sb.append("I supermercati che vuoi visitare sono: \n\n");

		for (String infoSup : scelteUtenti.get(chat_id).getListaSMdaVisitare()) {
		    sb.append(infoSup);
		    sb.append("\n\n");
		}
		sb.append("Se vuoi vedere cosa offre un supermercato nel suo ultimo volantino,"
			+ "relativamente ai prodotto che hai messo in lista clicca su *Offerte Volantino*.\n"
			+ "Quando hai finito di scegliere i supermarket e vuoi recarti al primo clicca su _Ok procediamo_.");
		new_message.setText(sb.toString());
		new_message.setParseMode("Markdown");

		// aggiungiamo la inline keyboard con i due comandi offerte e procediamo
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> listaComandi = new ArrayList<>();
		List<InlineKeyboardButton> riga = new ArrayList<>();
		riga.add(new InlineKeyboardButton().setText("Offerte sul volantino")
			.setCallbackData("Offerte sul volantino"));
		listaComandi.add(riga);
		riga = new ArrayList<>();
		riga.add(new InlineKeyboardButton().setText("Ok, procediamo 🛒").setCallbackData("Ok, procediamo"));
		listaComandi.add(riga);

		markupInline.setKeyboard(listaComandi);
		new_message.setReplyMarkup(markupInline);
	    }

	    try {
		smdb.execute(ack); // per risolvere il problema del loading su Android
		smdb.execute(new_message);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }
	}

	// Callback "sono al supermercato" quando si giunge ad un supermarket
	else if (call_data.equals("Sono al supermarket!")) {
	    try {
		smdb.execute(ack); // per risolvere il problema del loading su Android
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }
	    SendMessage mex = new SendMessage().setChatId(chat_id);
	    mex = creaMessaggioSpesaDelSMCorrente(scelteUtenti.get(chat_id), smdb, chat_id, mex);
	    smdb.inviaMessaggio(mex);
	}

	else if (ricevutoSupermarket(call_data)) {

	    String nome = call_data.substring(0, call_data.indexOf(','));
	    String indirizzo = call_data.substring(call_data.indexOf(',') + 2);

	    List<Offerta> offerteSupVicino = GestioneDB.estraiOfferteSupermarket(nome, indirizzo,
		    smdb.getlistaSpesaUtente(chat_id));

	    EditMessageText new_message = new EditMessageText().setChatId(chat_id).setMessageId((int) message_id);

	    StringBuilder sb = new StringBuilder();
	    sb.append("Le offerte relative al supermarket *" + nome + "* , in accordo alla tua lista, sono: \n\n");
	    for (Prodotto elementoListaSpesa : smdb.getlistaSpesaUtente(chat_id)) {
		sb.append("\n_" + elementoListaSpesa.getNome() + "_\n\n");
		for (Offerta offerta : offerteSupVicino) {
		    if (offerta.getProdotto().equals(elementoListaSpesa)) {
			sb.append("- " + offerta.getDescrizione() + ", prezzo: "
				+ String.format("%1.2f", offerta.getPrezzo()) + "\n");
		    }
		}
		sb.append("\n\n");
	    }

	    new_message.setText(sb.toString());
	    new_message.setParseMode("Markdown");

	    // aggiungiamo la inline keyboard con la lista dei volantini e il
	    // comando per tornare indietro
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> listaComandi = new ArrayList<>();
	    List<Supermarket> supermarketsVicini = calcolaSupermarketsVicini(
		    scelteUtenti.get(chat_id).getRaggioMassimo(), scelteUtenti.get(chat_id).getPosizione());

	    for (Supermarket sm : supermarketsVicini) {
		String testo = sm.getNome() + ", " + sm.getIndirizzo();
		List<InlineKeyboardButton> riga = new ArrayList<>();
		riga.add(new InlineKeyboardButton().setText(testo).setCallbackData(testo));
		listaComandi.add(riga);
	    }
	    String testo = "Indietro ◀";
	    List<InlineKeyboardButton> riga = new ArrayList<>();
	    riga.add(new InlineKeyboardButton().setText(testo).setCallbackData(testo));
	    listaComandi.add(riga);
	    markupInline.setKeyboard(listaComandi);
	    new_message.setReplyMarkup(markupInline);

	    try {
		smdb.execute(ack); // per risolvere il problema del loading su Android
		smdb.execute(new_message);
	    } catch (TelegramApiRequestException e) {
		// nulla può succedere se si sceglie lo stesso supermarket di cui si sta già
		// visualizzando le offerte
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }
	}
    }

    private static SendMessage creaMessaggioSpesaDelSMCorrente(InfoUtente infoUt, SupermarketDealBot smdb, long chat_id,
	    SendMessage mex) {
	StringBuilder sb = new StringBuilder();
	System.out.println(infoUt.getListaSMdaVisitare().size());
	String infoSM = infoUt.getListaSMdaVisitare().get(0);
	String nome = infoSM.substring(0, infoSM.indexOf(','));
	String indirizzo = infoSM.substring(infoSM.indexOf(',') + 2);
	List<Offerta> offerteSupVicino = GestioneDB.estraiOfferteSupermarket(nome, indirizzo,
		smdb.getlistaSpesaUtente(chat_id));

	sb.append("Le offerte relative al supermarket *" + nome + "* , in accordo alla tua lista, sono: \n\n");
	for (Prodotto elementoListaSpesa : smdb.getlistaSpesaUtente(chat_id)) {
	    sb.append("\n_" + elementoListaSpesa.getNome() + "_\n\n");
	    for (Offerta offerta : offerteSupVicino) {
		if (offerta.getProdotto().equals(elementoListaSpesa)) {
		    sb.append("- " + offerta.getDescrizione() + ", prezzo: "
			    + String.format("%1.2f", offerta.getPrezzo()) + "\n");
		}
	    }
	}

	sb.append("\n\nPuoi cancellare un elemento dalla tua lista premendoci sopra. "
		+ "Se vuoi visionare le offerte anche degli altri supermarket puoi premere il pulsante "
		+ "_Offerte sul volantino_ sotto. Se invece hai sbagliato ad eliminare un prodotto dalla "
		+ "lista puoi ripristinarla premendo su _Ripristina lista_. Quando hai finito di fare la spesa "
		+ "in questo supermarket possiamo passare al prossimo (se ce n'è uno) premendo su _Prossimo supermercato_. "
		+ "Buona Spesa!\n\n");
	mex.setText(sb.toString());

	// inseriamo la Custom Keyboard con i 3 bottoni appositi
	aggiungiCustomKeyboardPrincipale3Bott(mex, infoUt);

	mex.setParseMode("Markdown");
	smdb.inviaMessaggio(mex);

	// inseriamo la lista
	if (!smdb.getlistaSpesaUtente(chat_id).isEmpty()) {
	    mex = new SendMessage().setChatId(chat_id);
	    smdb.getLista(mex);
	}
	return mex;
    }

    // dobbiamo chiedere se si vuole recuperare la lista precedente oppure la spesa
    // è finita
    static void listaDellaSpesaVuota(Update update, Map<Long, InfoUtente> scelteUtenti, SupermarketDealBot smdb) {
	SendMessage mex = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId());
	mex.setText("Cosa vuoi fare?");
	// aggiungere i due bottoni ripristina lista e spesa finita
	ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
	List<KeyboardRow> keyboard = new ArrayList<>();
	KeyboardRow row = new KeyboardRow();
	row.add("Ripristina lista iniziale");
	keyboard.add(row);
	row = new KeyboardRow();
	row.add("🔝  Spesa Finita  🔝");
	keyboard.add(row);
	keyboardMarkup.setKeyboard(keyboard);
	keyboardMarkup.setResizeKeyboard(true);
	mex.setReplyMarkup(keyboardMarkup);

	smdb.inviaMessaggio(mex);
    }

}
