package bot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

class GestioneNegozianti {

	static SendMessage benvenutoNegoziante(Update update) {
		String testoBenvenuto = "Hai appena cliccato sulla gestione riservata ai negozianti. "
				+ "\nPer utilizzare gli strumenti inserisci il codice (password) a nove cifre corrispondente al tuo punto vendita."
				+ "\n\nPer richiedere assistenza, o il codice se sei un nuovo negoziante, contattami usando il pulsante qui sotto!";
		SendMessage mex = new SendMessage()
				.setChatId(update.getMessage().getChatId())
				.setText(testoBenvenuto);

		// creiamo la inline keyboard
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setUrl("https://t.me/domeCost")
				.setText("> Contattami <"));
		rowsInline.add(rowInline);
		markupInline.setKeyboard(rowsInline);
		mex.setReplyMarkup(markupInline);

		return mex;
	}

	static SendMessage menuStrumentiNegozianti(Update update) {
		String testoMessaggio = "Autenticazione avvenuta con successo! ✅\nScegli un'opzione dalla tastiera dedicata.";
		SendMessage mex = new SendMessage()
				.setChatId(update.getMessage().getChatId())
				.setText(testoMessaggio);

		// creiamo la custom keyboard per gli strumenti da negoziante
		inserisciCustKeyboardStrumentiNeg(mex);
		return mex;
	}

	static SendMessage codiceNonRiconosciuto(Update update) {
		SendMessage mex = new SendMessage()
				.setChatId(update.getMessage().getChatId()).setText(
						"Codice non riconosciuto ❌. Prova ad inserirlo nuovamente");

		// creiamo la inline keyboard
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setUrl("https://t.me/domeCost")
				.setText("> Contattami <"));
		rowsInline.add(rowInline);
		markupInline.setKeyboard(rowsInline);
		mex.setReplyMarkup(markupInline);

		return mex;
	}

	static boolean comandoAmmissibile(String testoMex) {
		return testoMex.equals("Inserisci nuovo punto vendita 🛒")
				|| testoMex.equals(
						"Modifica le informazioni del mio supermarket \u2139")
				|| testoMex.equals("Aggiungi nuove offerte 🏷")
				|| testoMex.equals("Info utili ❔") || testoMex.equals("Esci 🔚")
				|| testoMex.equals("No, torna indietro")
				|| testoMex.equals("Ok, dati corretti");
	}

	static boolean interazioneNegoziante(Update update) {
		String testoDomanda = update.getMessage().getReplyToMessage().getText();
		return testoDomanda.equals("Inserisci il nome del tuo supermarket:")
				|| testoDomanda
						.equals("Inserisci la città del tuo supermarket:")
				|| testoDomanda
						.equals("Inserisci l'indirizzo del tuo supermarket:")
				|| testoDomanda.equals(
						"Inserisci la posizione del tuo supermarket:\n\n"
								+ "Usando il formato <latitudine><spazio><longitudine> "
								+ "e usando il punto per i decimali, ad esempio:\n12.25482 0.688547")
				|| testoDomanda.equals(
						"Formato delle coordinate errate, inseriscile di nuovo.\n\n"
								+ "Inserisci la posizione del tuo supermarket:\n\n"
								+ "Usando il formato <latitudine><spazio><longitudine> "
								+ "e usando il punto per i decimali, ad esempio:\n12.25482 0.688547");

	}

	static SendMessage gestisciComandi(Update update, SupermarketDealBot smdb,
			Map<Long, Supermarket> modificheSupermercati) {
		SendMessage mex = new SendMessage()
				.setChatId(update.getMessage().getChatId());

		// comando "inserisci nuovo punto vendita"
		if (update.getMessage().getText()
				.equals("Inserisci nuovo punto vendita 🛒")) {
			// controlliamo che non sia presente già un supermarket associato al
			// codice negoziante
			if (GestioneDB.esisteSupermarket(update.getMessage().getChatId())) {
				String testoMessaggio = "Hai già inserito un supermercato! Ogni negoziante può inserirne solo uno. \n\n"
						+ "Le informazioni relative al tuo supermercato sono:\n";
				String infoSupermarket = GestioneDB
						.infoSupermarket(update.getMessage().getChatId());
				testoMessaggio += infoSupermarket;
				testoMessaggio += "\n\nPer favore clicca su *Modifica le informazioni* se vuoi "
						+ "aggiornare i dati oppure seleziona un altro comando.";
				mex.setText(testoMessaggio);
				mex.setParseMode("Markdown");

			} else {
				// il negoziante non ha associato nessun supermercato e può
				// inserirne uno, iniziando quindi un'interazione:
				String testoMessaggio = "Inserisci il nome del tuo supermarket:";
				mex.setReplyMarkup(new ForceReplyKeyboard())
						.setText(testoMessaggio);
			}
		}

		// comando "modifica informazioni supermarket"
		else if (update.getMessage().getText().equals(
				"Modifica le informazioni del mio supermarket \u2139")) {
			// controlliamo che il negoziante abbia un supermercato associato
			if (GestioneDB.esisteSupermarket(update.getMessage().getChatId())) {
				String testoMessaggio = "Le informazioni relative al tuo supermercato sono:\n";
				String infoSupermarket = GestioneDB
						.infoSupermarket(update.getMessage().getChatId());
				testoMessaggio += infoSupermarket;
				testoMessaggio += "\n\nPer favore inserisci i nuovi dati relativi al tuo "
						+ "supermercato, seguendo le istruzioni\n\n";
				mex.setText(testoMessaggio);
				mex.setReplyMarkup(new ForceReplyKeyboard())
						.setText(testoMessaggio);
				mex.setParseMode("Markdown");

				smdb.inviaMessaggio(mex);

				mex = new SendMessage()
						.setChatId(update.getMessage().getChatId());
				testoMessaggio = "Inserisci il nome del tuo supermarket:";
				mex.setText(testoMessaggio);
				mex.setReplyMarkup(new ForceReplyKeyboard())
						.setText(testoMessaggio);
				mex.setParseMode("Markdown");
			} else {
				// il negoziante non ha associato nessun supermercato
				String testoMessaggio = "Non è presente nessun supermercato associato a te. "
						+ "Seleziona un altro comando, ad esempio "
						+ "_Inserisci nuovo punto vendita_ per inserirne uno:";
				mex.setText(testoMessaggio);
				mex.setParseMode("Markdown");
			}

		}

		// comando "aggiungi nuove offerte"
		else if (update.getMessage().getText()
				.equals("Aggiungi nuove offerte 🏷")) {
			String testoMex = "Per modificare le _offerte_ che propone il tuo Supermarket devi inviarmi un file xls "
					+ "in un particolare formato, in modo che io possa aggiornare il database."
					+ "\n\nPuoi usare il file che ti invierò come *template*";
			mex.setText(testoMex);
			mex.setParseMode("Markdown");

			File fileXls = new File("src/main/resources/volantino.xls");
			SendDocument document = new SendDocument()
					.setChatId(update.getMessage().getChatId())
					.setDocument(fileXls);
			smdb.inviaMessaggioETemplateXls(mex, document);

			testoMex = "Se non ti è chiaro qualcosa sei libero di contattarmi usando il comando _info utili_. "
					+ "Quando sei pronto inviami il tuo file xls con le nuove offerte:";
			mex.setText(testoMex);
			mex.setParseMode("Markdown");
		}

		// comando "informazioni utili"
		else if (update.getMessage().getText().equals("Info utili ❔")) {
			String testoMex = "Questi sono gli strumenti di gestione riservati ai negozianti. "
					+ "\n\nPer richiedere assistenza, contattami cliccando su @domeCost";
			mex.setText(testoMex);
			inserisciCustKeyboardStrumentiNeg(mex);
		}

		// comando "esci"
		else if (update.getMessage().getText().equals("Esci 🔚")) {
			mex.setText(
					"Hai chiuso la gestione negozianti, puoi scegliere qualsiasi altro comando dal menu a tendina /:");
			ReplyKeyboardRemove remCK = new ReplyKeyboardRemove();
			mex.setReplyMarkup(remCK);
		}

		// Dati del supermercato modificati correttamente
		else if (update.getMessage().getText().equals("Ok, dati corretti")) {
			// provvediamo a modificare i dati su db usando l'hashmap
			if (GestioneDB.aggiornaSupermarket(modificheSupermercati
					.get(update.getMessage().getChatId()))) {
				mex.setText("Dati aggiornati correttamente!");
			} else {
				mex.setText(
						"Dati non aggiornati correttamente! Qualcosa è andato storto con il DB! "
								+ "Contattami tramite la sezione info utili!");
			}
			inserisciCustKeyboardStrumentiNeg(mex);
		}

		// Dati del supermercato non modificati, ritorna al menu
		else if (update.getMessage().getText().equals("No, torna indietro")) {
			modificheSupermercati.remove(update.getMessage().getChatId());
			mex.setText("Scegli un'opzione dalla tastiera dedicata:");
			inserisciCustKeyboardStrumentiNeg(mex);
		}

		return mex;
	}

	// questo metodo è usato per intercettare e gestire l'interazione con il
	// negoziante quando inserisce i dati relativi al suo supermarket
	static SendMessage gestisciInterazione(Update update,
			Map<Long, Supermarket> modificheSupermercati) {
		long chat_id = update.getMessage().getChatId();
		SendMessage mex = new SendMessage().setChatId(chat_id);

		String testoDomanda = update.getMessage().getReplyToMessage().getText();

		if (testoDomanda.equals("Inserisci il nome del tuo supermarket:")) {
			modificheSupermercati.put(chat_id, new Supermarket(chat_id));
			String nome = update.getMessage().getText();
			Supermarket sm = modificheSupermercati.get(chat_id);
			sm.setNome(nome);
			modificheSupermercati.put(chat_id, sm);

			String testoMessaggio = "Inserisci la città del tuo supermarket:";
			mex.setReplyMarkup(new ForceReplyKeyboard())
					.setText(testoMessaggio);
			mex.setText(testoMessaggio);
		}

		else if (testoDomanda
				.equals("Inserisci la città del tuo supermarket:")) {
			String citta = update.getMessage().getText();
			Supermarket sm = modificheSupermercati.get(chat_id);
			sm.setCitta(citta);
			modificheSupermercati.put(chat_id, sm);

			String testoMessaggio = "Inserisci l'indirizzo del tuo supermarket:";
			mex.setReplyMarkup(new ForceReplyKeyboard())
					.setText(testoMessaggio);
			mex.setText(testoMessaggio);
		}

		else if (testoDomanda
				.equals("Inserisci l'indirizzo del tuo supermarket:")) {
			String indirizzo = update.getMessage().getText();
			Supermarket sm = modificheSupermercati.get(chat_id);
			sm.setIndirizzo(indirizzo);
			modificheSupermercati.put(chat_id, sm);

			String testoMessaggio = "Inserisci la posizione del tuo supermarket:\n\n"
					+ "Usando il formato <_latitudine_><_spazio_><_longitudine_> "
					+ "e usando il _punto_ per i decimali, _ad esempio:_\n_12.25482_ _0.688547_";
			mex.setReplyMarkup(new ForceReplyKeyboard())
					.setText(testoMessaggio);
			mex.setText(testoMessaggio);
			mex.setParseMode("Markdown");
		}

		else if (testoDomanda
				.equals("Inserisci la posizione del tuo supermarket:\n\n"
						+ "Usando il formato <latitudine><spazio><longitudine> "
						+ "e usando il punto per i decimali, ad esempio:\n12.25482 0.688547")
				|| testoDomanda.equals(
						"Formato delle coordinate errate, inseriscile di nuovo.\n\n"
								+ "Inserisci la posizione del tuo supermarket:\n\n"
								+ "Usando il formato <latitudine><spazio><longitudine> "
								+ "e usando il punto per i decimali, ad esempio:\n12.25482 0.688547")) {
			String input = update.getMessage().getText();

			if (input.matches("[-+]?[0-9]*\\.?[0-9]+ [-+]?[0-9]*\\.?[0-9]+")) {
				String latitudine = input.substring(0, input.indexOf(" "));
				String longitudine = input.substring(input.indexOf(" "));
				float lat = Float.parseFloat(latitudine);
				float lon = Float.parseFloat(longitudine);

				Supermarket sm = modificheSupermercati.get(chat_id);
				sm.setLatitudine(lat);
				sm.setLongitudine(lon);
				modificheSupermercati.put(chat_id, sm);

				// messaggio che ricapitola i dati inseriti
				StringBuilder sb = new StringBuilder();
				sb.append(
						"Le *nuove* informazioni relative al tuo supermercato saranno:\n");
				sb.append("Nome = " + sm.getNome() + "\n");
				sb.append("Città = " + sm.getCitta() + "\n");
				sb.append("Indirizzo = " + sm.getIndirizzo() + "\n");
				sb.append("Posizione = <" + sm.getLatitudine() + "> <"
						+ sm.getLongitudine() + ">\n\n");
				sb.append(
						"Se vanno bene premi su _Ok_ altrimenti ritorna al menu");
				mex.setText(sb.toString());
				mex.setParseMode("Markdown");

				// inseriamo custom keyboard con due pulsanti
				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboard = new ArrayList<>();
				KeyboardRow row = new KeyboardRow();
				row.add("Ok, dati corretti");
				row.add("No, torna indietro");
				keyboard.add(row);
				keyboardMarkup.setKeyboard(keyboard);
				keyboardMarkup.setResizeKeyboard(true);
				mex.setReplyMarkup(keyboardMarkup);

			} else {
				// formato delle coordinate errate reinserirle
				String testoMessaggio = "Formato delle coordinate errate, inseriscile di nuovo.\n\n"
						+ "Inserisci la posizione del tuo supermarket:\n\n"
						+ "Usando il formato <_latitudine_><_spazio_><_longitudine_> "
						+ "e usando il _punto_ per i decimali, _ad esempio:_\n_12.25482_ _0.688547_";
				mex.setReplyMarkup(new ForceReplyKeyboard())
						.setText(testoMessaggio);
				mex.setText(testoMessaggio);
				mex.setParseMode("Markdown");
			}

		}

		return mex;
	}

	private static void inserisciCustKeyboardStrumentiNeg(SendMessage message) {
		// creiamo la custom keyboard per gli strumenti da negoziante
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		row.add("Inserisci nuovo punto vendita 🛒");
		keyboard.add(row);
		row = new KeyboardRow();
		row.add("Modifica le informazioni del mio supermarket \u2139");
		keyboard.add(row);
		row = new KeyboardRow();
		row.add("Aggiungi nuove offerte 🏷");
		keyboard.add(row);
		row = new KeyboardRow();
		row.add("Info utili	❔");
		row.add("Esci 🔚");
		keyboard.add(row);
		keyboardMarkup.setKeyboard(keyboard);
		keyboardMarkup.setResizeKeyboard(true);
		message.setReplyMarkup(keyboardMarkup);
	}

}
