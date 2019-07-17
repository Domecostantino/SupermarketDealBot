package bot;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class GestioneDB {

    private static Connection conn;

    static void DBConnection() throws ClassNotFoundException {

	Statement stmt = null;
	try {
	    conn = getConnection();
	    System.out.println("Opened database successfully");

	    stmt = conn.createStatement();
	    String sql = "CREATE TABLE IF NOT EXISTS LISTAPRODOTTI " + "(NOME VARCHAR(32) PRIMARY KEY     NOT NULL,"
		    + "POMODORI BOOL DEFAULT false," + "INSALATA BOOL DEFAULT false," + "PATATE BOOL DEFAULT false,"
		    + "OLIO BOOL DEFAULT false," + "PANE BOOL DEFAULT false," + "MERENDINE BOOL DEFAULT false,"
		    + "BISCOTTI BOOL DEFAULT false," + "BIRRE BOOL DEFAULT false," + "CAFFè BOOL DEFAULT false,"
		    + "PASTA BOOL DEFAULT false," + "RISO BOOL DEFAULT false," + "LATTE BOOL DEFAULT false,"
		    + "YOGURT BOOL DEFAULT false," + "TEA BOOL DEFAULT false," + "VINO BOOL DEFAULT false,"
		    + "FORMAGGI BOOL DEFAULT false," + "SALUMI BOOL DEFAULT false," + "SCATOLAME BOOL DEFAULT false,"
		    + "SURGELATI BOOL DEFAULT false," + "VERDURE BOOL DEFAULT false," + "SNACKS BOOL DEFAULT false,"
		    + "ALCOLICI BOOL DEFAULT false," + "AGRUMI BOOL DEFAULT false," + "ACQUA BOOL DEFAULT false,"
		    + "CARNE BOOL DEFAULT false," + "PESCE BOOL DEFAULT false," + "DETERGENTI BOOL DEFAULT false);";

	    stmt.executeUpdate(sql);
	    stmt.close();
	} catch (Exception e) {
	    System.err.println(e.getClass().getName() + ": " + e.getMessage());
	    System.exit(0);
	}
	System.out.println("Table created successfully");
    }

    static Connection getConnection() throws URISyntaxException, SQLException {

	URI dbUri = new URI(System.getenv("DATABASE_URL"));

	String username = dbUri.getUserInfo().split(":")[0];
	String password = dbUri.getUserInfo().split(":")[1];
	String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
		+ "?sslmode=require";

	return DriverManager.getConnection(dbUrl, username, password);
    }

    static void aggiugiUtente(long chat_id) {
	try {
	    Statement stmt = conn.createStatement();
	    String sql = "INSERT INTO LISTAPRODOTTI (NOME) VALUES ('" + chat_id + "');";
	    stmt.executeUpdate(sql);
	    stmt.close();
	} catch (SQLException e) {
	    System.err.println(e);
	}

    }

    static List<Prodotto> ripristinaUtente(long chat_id) {
	List<Prodotto> listaProdotti = new ArrayList<>();
	try {
	    Statement stmt = conn.createStatement();
	    String sql = "SELECT * FROM LISTAPRODOTTI WHERE NOME = '" + chat_id + "';";
	    ResultSet rs = stmt.executeQuery(sql);
	    rs.next();
	    if (rs.getBoolean("Pomodori"))
		listaProdotti.add(new Prodotto("Pomodori"));
	    if (rs.getBoolean("Insalata"))
		listaProdotti.add(new Prodotto("Insalata"));
	    if (rs.getBoolean("Patate"))
		listaProdotti.add(new Prodotto("Patate"));
	    if (rs.getBoolean("Olio"))
		listaProdotti.add(new Prodotto("Olio"));
	    if (rs.getBoolean("Pane"))
		listaProdotti.add(new Prodotto("Pane"));
	    if (rs.getBoolean("Merendine"))
		listaProdotti.add(new Prodotto("Merendine"));
	    if (rs.getBoolean("Biscotti"))
		listaProdotti.add(new Prodotto("Biscotti"));
	    if (rs.getBoolean("Birre"))
		listaProdotti.add(new Prodotto("Birre"));
	    if (rs.getBoolean("Caffè"))
		listaProdotti.add(new Prodotto("Caffè"));
	    if (rs.getBoolean("Pasta"))
		listaProdotti.add(new Prodotto("Pasta"));
	    if (rs.getBoolean("Riso"))
		listaProdotti.add(new Prodotto("Riso"));
	    if (rs.getBoolean("Latte"))
		listaProdotti.add(new Prodotto("Latte"));
	    if (rs.getBoolean("Yogurt"))
		listaProdotti.add(new Prodotto("Yogurt"));
	    if (rs.getBoolean("Tea"))
		listaProdotti.add(new Prodotto("Tea"));
	    if (rs.getBoolean("Vino"))
		listaProdotti.add(new Prodotto("Vino"));
	    if (rs.getBoolean("Formaggi"))
		listaProdotti.add(new Prodotto("Formaggi"));
	    if (rs.getBoolean("Salumi"))
		listaProdotti.add(new Prodotto("Salumi"));
	    if (rs.getBoolean("Scatolame"))
		listaProdotti.add(new Prodotto("Scatolame"));
	    if (rs.getBoolean("Surgelati"))
		listaProdotti.add(new Prodotto("Surgelati"));
	    if (rs.getBoolean("Verdure"))
		listaProdotti.add(new Prodotto("Verdure"));
	    if (rs.getBoolean("Snacks"))
		listaProdotti.add(new Prodotto("Snacks"));
	    if (rs.getBoolean("Alcolici"))
		listaProdotti.add(new Prodotto("Alcolici"));
	    if (rs.getBoolean("Agrumi"))
		listaProdotti.add(new Prodotto("Agrumi"));
	    if (rs.getBoolean("Acqua"))
		listaProdotti.add(new Prodotto("Acqua"));
	    if (rs.getBoolean("Carne"))
		listaProdotti.add(new Prodotto("Carne"));
	    if (rs.getBoolean("Detergenti"))
		listaProdotti.add(new Prodotto("Detergenti"));
	    if (rs.getBoolean("Pesce"))
		listaProdotti.add(new Prodotto("Pesce"));
	    rs.close();
	    stmt.close();
	} catch (SQLException e) {
	    System.err.println(e);
	}
	return listaProdotti;

    }

    static Map<Long, List<Prodotto>> ripristinaListeUtenti() {
	Map<Long, List<Prodotto>> mappa = new HashMap<>();
	try {
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM LISTAPRODOTTI;");
	    while (rs.next()) {
		String chat_id = rs.getString("nome");
		List<Prodotto> listaProdotti = new ArrayList<>();
		if (rs.getBoolean("Pomodori"))
		    listaProdotti.add(new Prodotto("Pomodori"));
		if (rs.getBoolean("Insalata"))
		    listaProdotti.add(new Prodotto("Insalata"));
		if (rs.getBoolean("Patate"))
		    listaProdotti.add(new Prodotto("Patate"));
		if (rs.getBoolean("Olio"))
		    listaProdotti.add(new Prodotto("Olio"));
		if (rs.getBoolean("Pane"))
		    listaProdotti.add(new Prodotto("Pane"));
		if (rs.getBoolean("Merendine"))
		    listaProdotti.add(new Prodotto("Merendine"));
		if (rs.getBoolean("Biscotti"))
		    listaProdotti.add(new Prodotto("Biscotti"));
		if (rs.getBoolean("Birre"))
		    listaProdotti.add(new Prodotto("Birre"));
		if (rs.getBoolean("Caffè"))
		    listaProdotti.add(new Prodotto("Caffè"));
		if (rs.getBoolean("Pasta"))
		    listaProdotti.add(new Prodotto("Pasta"));
		if (rs.getBoolean("Riso"))
		    listaProdotti.add(new Prodotto("Riso"));
		if (rs.getBoolean("Latte"))
		    listaProdotti.add(new Prodotto("Latte"));
		if (rs.getBoolean("Yogurt"))
		    listaProdotti.add(new Prodotto("Yogurt"));
		if (rs.getBoolean("Tea"))
		    listaProdotti.add(new Prodotto("Tea"));
		if (rs.getBoolean("Vino"))
		    listaProdotti.add(new Prodotto("Vino"));
		if (rs.getBoolean("Formaggi"))
		    listaProdotti.add(new Prodotto("Formaggi"));
		if (rs.getBoolean("Salumi"))
		    listaProdotti.add(new Prodotto("Salumi"));
		if (rs.getBoolean("Scatolame"))
		    listaProdotti.add(new Prodotto("Scatolame"));
		if (rs.getBoolean("Surgelati"))
		    listaProdotti.add(new Prodotto("Surgelati"));
		if (rs.getBoolean("Verdure"))
		    listaProdotti.add(new Prodotto("Verdure"));
		if (rs.getBoolean("Snacks"))
		    listaProdotti.add(new Prodotto("Snacks"));
		if (rs.getBoolean("Alcolici"))
		    listaProdotti.add(new Prodotto("Alcolici"));
		if (rs.getBoolean("Agrumi"))
		    listaProdotti.add(new Prodotto("Agrumi"));
		if (rs.getBoolean("Acqua"))
		    listaProdotti.add(new Prodotto("Acqua"));
		if (rs.getBoolean("Carne"))
		    listaProdotti.add(new Prodotto("Carne"));
		if (rs.getBoolean("Detergenti"))
		    listaProdotti.add(new Prodotto("Detergenti"));
		if (rs.getBoolean("Pesce"))
		    listaProdotti.add(new Prodotto("Pesce"));

		mappa.put(Long.parseLong(chat_id), listaProdotti);
	    }
	    rs.close();
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return mappa;
    }

    static HashSet<Integer> ripristinaNegoziantiAbilitati() {
	HashSet<Integer> negozianti = new HashSet<>();
	try {
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM NEGOZIANTI;");
	    while (rs.next()) {
		Integer chat_id = rs.getInt("codiceNegoziante");
		negozianti.add(chat_id);
	    }
	    rs.close();
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return negozianti;
    }

    static void svuotaListaUtente(long chat_id) {
	try {
	    Statement stmt = conn.createStatement();
	    String sql = "DELETE FROM LISTAPRODOTTI WHERE NOME='" + chat_id + "';";
	    stmt.executeUpdate(sql);
	    sql = "INSERT INTO LISTAPRODOTTI (NOME) VALUES ('" + chat_id + "');";
	    stmt.executeUpdate(sql);
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static void aggiungiProdotto(Long chat_id, String nomeP) {
	try {
	    Statement stmt = conn.createStatement();
	    String sql = "UPDATE LISTAPRODOTTI SET " + nomeP + " = TRUE WHERE NOME = '" + chat_id + "';";
	    stmt.executeUpdate(sql);
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static void rimuoviProdotto(long chat_id, String nomeP) {
	try {
	    Statement stmt = conn.createStatement();
	    String sql = "UPDATE LISTAPRODOTTI SET " + nomeP + " = FALSE WHERE NOME = '" + chat_id + "';";
	    stmt.executeUpdate(sql);
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static boolean esisteSupermarket(Long chatId) {
	boolean result = false;
	try {
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM SUPERMARKETS WHERE id = '" + chatId + "';");
	    rs.next();
	    if (rs.getInt(1) > 0)
		result = true;
	    rs.close();
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return result;
    }

    static String infoSupermarket(Long chatId) {
	String result = "";
	try {
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM SUPERMARKETS WHERE id = '" + chatId + "';");
	    StringBuilder sb = new StringBuilder();
	    rs.next();
	    sb.append("_Nome_ = ");
	    sb.append(rs.getString("nome"));
	    sb.append("\n");
	    sb.append("_Città_ = ");
	    sb.append(rs.getString("citta"));
	    sb.append("\n");
	    sb.append("_Indirizzo_ = ");
	    sb.append(rs.getString("indirizzo"));
	    sb.append("\n");
	    sb.append("_Posizione_ = ");
	    sb.append(rs.getDouble("latitudine"));
	    sb.append(", ");
	    sb.append(rs.getDouble("longitudine"));
	    result = sb.toString();
	    rs.close();
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return result;
    }

    static Supermarket posizioneSupermarket(String nome) {
	Supermarket sm = new Supermarket(0);
	try {
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM SUPERMARKETS WHERE nome = '" + nome + "';");
	    rs.next();
	    sm.setLatitudine((float) rs.getDouble("latitudine"));
	    sm.setLongitudine((float) rs.getDouble("longitudine"));
	    rs.close();
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return sm;
    }

    static List<Supermarket> listaSupermercati() {
	List<Supermarket> lista = new LinkedList<>();
	try {
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM SUPERMARKETS;");
	    while (rs.next()) {
		int chat_id = rs.getInt("id");
		String nome = rs.getString("nome");
		String citta = rs.getString("citta");
		String ind = rs.getString("indirizzo");
		float lat = rs.getFloat("latitudine");
		float longi = rs.getFloat("longitudine");
		Supermarket sm = new Supermarket(chat_id, nome, citta, ind, lat, longi);
		lista.add(sm);
	    }
	    rs.close();
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return lista;
    }

    static boolean aggiornaSupermarket(Supermarket supermarket) {
	try {
	    Statement stmt = conn.createStatement();
	    // prima si eliminano dal db il supermarket precedentemente salvato
	    // (conviene rispetto ad update)
	    String sql = "DELETE FROM SUPERMARKETS WHERE ID = " + supermarket.getProprietario() + ";";
	    stmt.executeUpdate(sql);

	    // poi inseriamo la nuova tupla
	    sql = "INSERT INTO SUPERMARKETS (ID,NOME,CITTA,INDIRIZZO,LATITUDINE,LONGITUDINE) " + "VALUES ("
		    + supermarket.getProprietario() + ", '" + supermarket.getNome() + "', '" + supermarket.getCitta()
		    + "', '" + supermarket.getIndirizzo() + "', " + supermarket.getLatitudine() + ", "
		    + supermarket.getLongitudine() + ");";
	    stmt.executeUpdate(sql);
	    stmt.close();
	    return true;
	} catch (Exception e) {
	    System.err.println(e);
	    return false;
	}
    }

    static boolean aggiornaOfferte(int idSuperm, List<Offerta> listaOfferte) {
	try {
	    Statement stmt = conn.createStatement();
	    // prima si eliminano dal db le offerte precedenti del supermercato
	    String sql = "DELETE FROM OFFERTE WHERE SUPERMARKET = " + idSuperm + ";";
	    stmt.executeUpdate(sql);

	    // poi inseriamo le nuove offerte
	    for (Offerta offerta : listaOfferte) {
		sql = "INSERT INTO OFFERTE (SUPERMARKET,PREZZO,CATEGORIA,DESCRIZIONE) " + "VALUES ("
			+ offerta.getIdSupermercato() + ", " + offerta.getPrezzo() + ", '"
			+ offerta.getProdotto().getNome() + "', '" + offerta.getDescrizione() + "');";
		stmt.executeUpdate(sql);
	    }
	    stmt.close();
	    return true;
	} catch (Exception e) {
	    System.err.println(e);
	    return false;
	}
    }

    static List<Offerta> estraiOfferteSupermarket(String nome, String indirizzo, List<Prodotto> listaPreferenze) {
	List<Offerta> offerteVol = new LinkedList<>();
	long idSuper;
	try {
	    Statement stmt = conn.createStatement();

	    // recuperiamo l'id del supermercato dati il nome l'indirizzo
	    String sql = "SELECT ID FROM SUPERMARKETS WHERE NOME = '" + nome + "' AND INDIRIZZO = '" + indirizzo + "';";
	    ResultSet rs = stmt.executeQuery(sql);
	    rs.next();
	    idSuper = Long.parseLong(rs.getString(1));

	    // ora recuperiamo le offerte in accordo alla lista dell'utente
	    for (Prodotto categoria : listaPreferenze) {
		sql = "SELECT * FROM OFFERTE WHERE SUPERMARKET = " + idSuper + " AND CATEGORIA = '"
			+ categoria.getNome() + "';";
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
		    Prodotto prod = new Prodotto(categoria.getNome());
		    double prezzo = rs.getDouble("prezzo");
		    String descr = rs.getString("descrizione");
		    Offerta off = new Offerta(prod, prezzo, descr);
		    offerteVol.add(off);
		}
	    }
	    stmt.close();
	    return offerteVol;
	} catch (Exception e) {
	    System.err.println(e);
	    return null;
	}
    }

    static boolean offertePresentiNelProssimoSM(String nomeProsSM, List<Prodotto> listaUtenteCorrente) {
	try {
	    Statement stmt = conn.createStatement();
	    long idSuper;
	    // recuperiamo l'id del supermercato dato il nome
	    String sql = "SELECT ID FROM SUPERMARKETS WHERE NOME = '" + nomeProsSM + "';";
	    ResultSet rs = stmt.executeQuery(sql);
	    rs.next();
	    idSuper = Long.parseLong(rs.getString(1));

	    // ora recuperiamo le offerte del prossimo supermarket
	    sql = "SELECT * FROM OFFERTE WHERE SUPERMARKET = " + idSuper + ";";
	    rs = stmt.executeQuery(sql);
	    // verifichiamo se c'è almeno un'offerta relativa alla lista dell'utente
	    while (rs.next()) {
		if (listaUtenteCorrente.contains(new Prodotto(rs.getString("categoria"))))
		    return true;
	    }
	    stmt.close();
	    return false;
	} catch (Exception e) {
	    System.err.println(e);
	    return false;
	}
    }

    static void chiudiConn() {
	try {
	    conn.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}

    }

}
