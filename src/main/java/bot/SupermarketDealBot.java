package bot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SupermarketDealBot extends TelegramLongPollingBot {

    private Map<Long, List<Prodotto>> listeUtenti;
    private HashSet<Integer> negoziantiAbilitati;
    private Map<Long, Supermarket> modificheSupermercati;
    private Map<Long, InfoUtente> scelteUtenti;

    public SupermarketDealBot() {
	super();

	// set-up della connessione con il db
	try {
	    GestioneDB.DBConnection();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}

	// ripristiniamo la lista degli utenti in caso di crash
	listeUtenti = GestioneDB.ripristinaListeUtenti();

	// ripristiniamo la lista dei negozianti abilitati
	negoziantiAbilitati = GestioneDB.ripristinaNegoziantiAbilitati();

	modificheSupermercati = new HashMap<>();
	scelteUtenti = new HashMap<>();
    }

    public void onUpdateReceived(Update update) {

	// Controlliamo che il messaggio ci sia e abbia testo
	if (update.hasMessage() && update.getMessage().hasText()) {
	    long chat_id = update.getMessage().getChatId();
	    System.out.println(chat_id);

	    // Gestiamo il caso di utilizzo di un comando
	    if (update.getMessage().getText().charAt(0) == '/') {

		// Comando /start
		if (update.getMessage().getText().equals("/start")) {

		    // creazione o ripristino di una lista per un utente
		    if (listeUtenti.containsKey(chat_id)) {
			listeUtenti.put(chat_id, GestioneDB.ripristinaUtente(chat_id));
		    } else {
			GestioneDB.aggiugiUtente(chat_id);
			listeUtenti.put(chat_id, new ArrayList<>());
		    }

		    // eliminiamo eventuali custom keyboard ancora aperte
		    SendMessage message = new SendMessage().setChatId(chat_id)
			    .setText("Salve! Hai appena avviato SupermarketDealBot,"
				    + " il bot telegram che ti aiuta con la spesa!\n\nSono presenti diversi comandi"
				    + " tutti richiamabili usando il tasto '/' oppure il menu a tendina.");
		    ReplyKeyboardRemove remCK = new ReplyKeyboardRemove();
		    message.setReplyMarkup(remCK);
		    Message sent = null;
		    try {
			sent = execute(message);
		    } catch (TelegramApiException e) {
			e.printStackTrace();
		    }
		    DeleteMessage d = new DeleteMessage().setChatId(chat_id).setMessageId(sent.getMessageId());
		    try {
			execute(d);
		    } catch (TelegramApiException e) {
			e.printStackTrace();
		    }

		    // creazione inline keyboard messaggio di benvenuto
		    SendMessage s = new SendMessage().setChatId(chat_id)
			    .setText("Salve! Hai appena avviato SupermarketDealBot,"
				    + " il bot telegram che ti aiuta con la spesa!\n\nSono presenti diversi comandi"
				    + " tutti richiamabili usando il tasto '/' oppure il menu a tendina.");
		    messaggioBenvenuto(s);
		    inviaMessaggio(s);
		}

		// Comando /lista
		else if (update.getMessage().getText().equals("/lista")) {
		    SendMessage message = new SendMessage().setChatId(chat_id);
		    getLista(message);
		    inviaMessaggio(message);
		}

		// Comando /aggiungi
		else if (update.getMessage().getText().equals("/aggiungi")) {
		    String answer = "Seleziona il prodotto che vuoi acquistare oppure premi 'Finito'";
		    SendMessage message = new SendMessage().setChatId(chat_id).setText(answer);
		    GestioneAddProdotti.aperturaCustumKeyboard(message);
		    inviaMessaggio(message);
		}

		// Comando /svuota-lista
		else if (update.getMessage().getText().equals("/svuota_lista")) {
		    String answer = "La lista è stata azzerata";
		    listeUtenti.get(chat_id).clear();
		    GestioneDB.svuotaListaUtente(chat_id);
		    SendMessage message = new SendMessage().setChatId(chat_id).setText(answer);
		    inviaMessaggio(message);
		}

		// Comando /venditore
		else if (update.getMessage().getText().equals("/negozianti")) {
		    SendMessage message = GestioneNegozianti.benvenutoNegoziante(update);
		    inviaMessaggio(message);
		}

		// Comando /spesa_ottimizzata
		else if (update.getMessage().getText().equals("/spesa_ottimizzata")) {
		    SendMessage message;
		    if (listeUtenti.get(chat_id).isEmpty()) {
			message = new SendMessage().setChatId(chat_id)
				.setText("La tua lista è vuota! "
					+ "Per usare questo comando devi prima creare la tua lista della spesa."
					+ "\n\nPuoi usare il comando /aggiungi.");
		    } else {
			message = GestioneSpesa.messaggioBenvenuto(update);
		    }
		    inviaMessaggio(message);
		}

		// Comando /info
		else if (update.getMessage().getText().equals("/info")) {
		    SendMessage message = new SendMessage().setChatId(chat_id)
			    .setText("Questo bot ti aiuta con le offerte della spesa 🛒\n\n"
				    + "Progetto didattico per il corso di Sistemi Distribuiti, "
				    + "DIMES - UniCal\n\n Author @domeCost");
		    inviaMessaggio(message);
		}

	    }

	    else { // ricevuto un messaggio che non è un comando

		SendMessage message = null;
		String testoMex = update.getMessage().getText();

		// gestiamo i possibili comandi inviati dalla custom
		// keyboard dei negozianti e le possibili interazioni
		if (GestioneNegozianti.comandoAmmissibile(testoMex)) {
		    message = GestioneNegozianti.gestisciComandi(update, this, modificheSupermercati);
		} else if (update.getMessage().isReply() && GestioneNegozianti.interazioneNegoziante(update)) {
		    message = GestioneNegozianti.gestisciInterazione(update, modificheSupermercati);
		}

		// gestiamo il caso di interazione per la spesa ottimizzata
		// (custom Keyboards e altro)
		else if (GestioneSpesa.interazioneUtente(testoMex)) {
		    message = GestioneSpesa.gestisciInterazione(update, scelteUtenti, this);
		}

		else if (testoMex.matches("\\w+\\s\\W+") || testoMex.equals("Caffè ☕")) {
		    // inserimento di un prodotto in seguito alla scelta su
		    // Custom keyboard
		    message = GestioneAddProdotti.gestioneNuovoProd(listeUtenti, update);

		} else if (testoMex.matches("\\d{9}")) {
		    // intero di nove cifre, vediamo se combacia con la
		    // password per negozianti corrispondente all'utente
		    if (negoziantiAbilitati.contains(Integer.parseInt(testoMex))
			    && chat_id == Long.parseLong(testoMex)) {
			message = GestioneNegozianti.menuStrumentiNegozianti(update);
		    } else {
			message = GestioneNegozianti.codiceNonRiconosciuto(update);
		    }
		}

		else { // comando non riconosciuto
		    message = new SendMessage().setChatId(chat_id).setText("Comando non riconosciuto.");
		}
		inviaMessaggio(message);
	    }
	}

	// Messaggio di Callback senza testo generato dalla inline keyboard
	else if (update.hasCallbackQuery()) {
	    gestisciCallback(update);

	}

	// messaggio contente la posizione
	else if (update.getMessage().hasLocation()) {
	    SendMessage message = GestioneSpesa.inviataPosizione(update, scelteUtenti);
	    inviaMessaggio(message);
	}

	// messaggio contenente il file xls per l'aggiornamento delle offerte
	else if (update.getMessage().getDocument().getMimeType().equals("application/vnd.ms-excel")) {
	    // ricezione del file xls
	    File tmp = null;
	    try {
		tmp = downloadFile(getFilePath(update.getMessage().getDocument()));
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }
	    System.out.println(tmp.getAbsolutePath());

	    List<Offerta> PROV = Utils.leggiVolantinoXls(tmp);
	    System.out.println(PROV);
	    int id = PROV.get(0).getIdSupermercato();

	    SendMessage mex = new SendMessage().setChatId(update.getMessage().getChatId());
	    if (id == update.getMessage().getChatId() && GestioneDB.aggiornaOfferte(id, PROV)
		    && GestioneDB.esisteSupermarket((long) id)) {
		String testoMex = "Lista delle offerte aggiornata correttamente!";
		mex.setText(testoMex);
	    } else if (!GestioneDB.esisteSupermarket((long) id)) {
		String testoMex = "Lista delle offerte non aggiornata! "
			+ "Non risulta nessun supermarket associato al tuo codice. "
			+ "Devi prima creare un punto vendita usando l'apposito comando. ";
		mex.setText(testoMex);
	    } else if (id != update.getMessage().getChatId()) {
		String testoMex = "Lista delle offerte non aggiornata! Non hai formattato "
			+ "bene il file xls, il codice del supermercato con corrisponde al tuo codice negoziante.";
		mex.setText(testoMex);
	    } else {
		String testoMex = "Lista delle offerte non aggiornata! Errore con il db!";
		mex.setText(testoMex);
	    }
	    inviaMessaggio(mex);
	}
    }

    void getLista(SendMessage message) {
	List<Prodotto> listaProdotti = listeUtenti.get(Long.parseLong(message.getChatId()));
	if (listaProdotti.isEmpty()) {
	    message.setText("Lista vuota! usa il comando /aggiungi per aggiungere un prodotto");
	    return;
	}
	message.setText("Lista (clicca su un prodotto per rimuoverlo):");
	int i = 0;

	InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
	List<InlineKeyboardButton> rowInline = new ArrayList<>();

	for (Prodotto p : listaProdotti) {
	    rowInline.add(
		    new InlineKeyboardButton().setText(p.getNome() + " " + p.getEmoji()).setCallbackData(p.getNome()));
	    i++;
	    if (i % 2 == 0) {
		rowsInline.add(rowInline);
		rowInline = new ArrayList<>();
	    }
	}
	if (i % 2 != 0)
	    rowsInline.add(rowInline);

	// Add it to the message
	markupInline.setKeyboard(rowsInline);
	message.setReplyMarkup(markupInline);
    }

    void inviaMessaggio(SendMessage m) {
	try {
	    execute(m);
	} catch (TelegramApiException e) {
	    e.printStackTrace();
	}
    }

    void inviaMessaggioETemplateXls(SendMessage mexTesto, SendDocument fileXls) {
	try {
	    execute(mexTesto);
	    execute(fileXls);
	} catch (TelegramApiException e) {
	    e.printStackTrace();
	}
    }

    List<Prodotto> getlistaSpesaUtente(long id_utente) {
	return listeUtenti.get(id_utente);
    }

    void setlistaSpesaUtente(long id_utente, List<Prodotto> nuovaListaSpesa) {
	listeUtenti.put(id_utente, nuovaListaSpesa);
    }

    private void messaggioBenvenuto(SendMessage message) {
	InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
	List<InlineKeyboardButton> rowInline = new ArrayList<>();
	rowInline.add(new InlineKeyboardButton().setText("▻").setCallbackData("freccia 1 ▻"));
	// Set the keyboard to the markup
	rowsInline.add(rowInline);
	// Add it to the message
	markupInline.setKeyboard(rowsInline);
	message.setReplyMarkup(markupInline);
    }

    private void gestisciCallback(Update update) {
	String call_data = update.getCallbackQuery().getData();
	long message_id = update.getCallbackQuery().getMessage().getMessageId();
	long chat_id = update.getCallbackQuery().getMessage().getChatId();

	// per risolvere il problema del loading su Android
	AnswerCallbackQuery ack = new AnswerCallbackQuery();
	ack.setCallbackQueryId(update.getCallbackQuery().getId());

	// Ora si gestiscono le varie callbacks
	if (call_data.equals("freccia 1 ▻") || call_data.equals("freccia 3 ◅")) {
	    String answer = "La principale funzionalità del bot è della di creare "
		    + "una /lista della spesa interattiva e di guidarti alla scoperta "
		    + "delle offerte presenti nei volantini dei Supermarket presenti nella"
		    + " tua zona, usando il comando /spesa_ottimizzata.";
	    EditMessageText new_message = new EditMessageText().setChatId(chat_id).setMessageId((int) message_id)
		    .setText(answer);
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
	    List<InlineKeyboardButton> rowInline = new ArrayList<>();
	    rowInline.add(new InlineKeyboardButton().setText("◅").setCallbackData("freccia 2 ◅"));
	    rowInline.add(new InlineKeyboardButton().setText("▻").setCallbackData("freccia 2 ▻"));
	    // Set the keyboard to the markup
	    rowsInline.add(rowInline);
	    // Add it to the message
	    markupInline.setKeyboard(rowsInline);
	    new_message.setReplyMarkup(markupInline);

	    try {
		execute(ack); // per risolvere il problema del loading su
			      // Android
		execute(new_message);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }

	} else if (call_data.equals("freccia 2 ▻")) {
	    String answer = "Per i responsabili dei punti vendita sono presenti "
		    + "appositi comandi richiamabili tramite /negozianti.\n\nComponi la "
		    + "tua lista ed esplora tutte le funzionalità, buona spesa con SupermarketDealBot! 🛒.";
	    EditMessageText new_message = new EditMessageText().setChatId(chat_id).setMessageId((int) message_id)
		    .setText(answer);
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
	    List<InlineKeyboardButton> rowInline = new ArrayList<>();
	    rowInline.add(new InlineKeyboardButton().setText("◅").setCallbackData("freccia 3 ◅"));
	    // Set the keyboard to the markup
	    rowsInline.add(rowInline);
	    // Add it to the message
	    markupInline.setKeyboard(rowsInline);
	    new_message.setReplyMarkup(markupInline);
	    try {
		execute(ack);
		execute(new_message);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }
	} else if (call_data.equals("freccia 2 ◅")) {
	    String answer = "Salve! Hai appena avviato SupermarketDealBot,"
		    + " il bot telegram che ti aiuta con la spesa!\n\nSono presenti diversi comandi"
		    + " tutti richiamabili usando il tasto '/' oppure il menu a tendina.";
	    EditMessageText new_message = new EditMessageText().setChatId(chat_id).setMessageId((int) message_id)
		    .setText(answer);
	    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
	    List<InlineKeyboardButton> rowInline = new ArrayList<>();
	    rowInline.add(new InlineKeyboardButton().setText("▻").setCallbackData("freccia 1 ▻"));
	    // Set the keyboard to the markup
	    rowsInline.add(rowInline);
	    // Add it to the message
	    markupInline.setKeyboard(rowsInline);
	    new_message.setReplyMarkup(markupInline);
	    try {
		execute(ack);
		execute(new_message);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }
	}

	else if (call_data.equals("Done")) {
	    String answer = "Lista aggiornata! Visualizzala con /lista";
	    SendMessage new_message = new SendMessage().setChatId(chat_id).setText(answer);

	    ReplyKeyboardRemove rem_custkeyboard = new ReplyKeyboardRemove();
	    new_message.setReplyMarkup(rem_custkeyboard);

	    try {
		execute(ack);
		execute(new_message);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }
	}

	else if (Utils.prodottoAmmissibile(call_data)) {

	    EditMessageText new_message = new EditMessageText();

	    List<Prodotto> listaProdotti = listeUtenti.get(chat_id);

	    listaProdotti.remove(new Prodotto(call_data));
	    GestioneDB.rimuoviProdotto(chat_id, call_data);

	    if (listaProdotti.isEmpty()) {
		// stiamo modificando la lista nella spesa ottimizzata
		if (scelteUtenti.containsKey(chat_id) && scelteUtenti.get(chat_id).isFaseFinaleSpesa()) {
		    new_message.setChatId(chat_id).setMessageId((int) message_id).setText("Lista della spesa vuota.");
		    try {
			execute(ack);
			execute(new_message);
		    } catch (TelegramApiException e) {
			e.printStackTrace();
		    }
		    GestioneSpesa.listaDellaSpesaVuota(update, scelteUtenti, this);
		    return;
		} else {
		    new_message.setChatId(chat_id).setMessageId((int) message_id)
			    .setText("Lista vuota! usa il comando /aggiungi per aggiungere un prodotto");
		}
	    } else {
		new_message.setChatId(chat_id).setMessageId((int) message_id)
			.setText("Lista (clicca su un prodotto per rimuoverlo):");

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		int i = 0;

		for (Prodotto p : listaProdotti) {
		    rowInline.add(new InlineKeyboardButton().setText(p.getNome() + " " + p.getEmoji())
			    .setCallbackData(p.getNome()));
		    i++;
		    if (i % 2 == 0) {
			rowsInline.add(rowInline);
			rowInline = new ArrayList<>();
		    }
		}
		if (i % 2 != 0)
		    rowsInline.add(rowInline);

		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		new_message.setReplyMarkup(markupInline);
	    }
	    try {
		execute(ack);
		execute(new_message);
	    } catch (TelegramApiException e) {
		e.printStackTrace();
	    }
	}

	// callback nell'interazione con l'utente durante spesa_ottimizzata
	else if (GestioneSpesa.callbackGestioneSpesa(call_data)) {
	    GestioneSpesa.gestisciCallback(update, scelteUtenti, this);
	}
    }

    private String getFilePath(final Document doc) {
	final GetFile getFileMethod = new GetFile();
	getFileMethod.setFileId(doc.getFileId());
	try {
	    final org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);
	    System.out.println(file.getFilePath());
	    return file.getFilePath();
	} catch (final TelegramApiException e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public String getBotUsername() {
	return "Supermarket_deal_bot";
    }

    @Override
    public String getBotToken() {
	return "-----------------------------------";
    }

    @Override
    public void onClosing() {
	super.onClosing();
	GestioneDB.chiudiConn();
    }
}
