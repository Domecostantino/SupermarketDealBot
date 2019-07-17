package bot;

import java.util.*;

import org.telegram.telegrambots.meta.api.objects.Location;

class InfoUtente {
	private int raggioMassimo;
	private Location posizione;
	private List<String> listaSMdaVisitare = new LinkedList<>();
	private List<String> listaUtenteIniziale = new LinkedList<>();
	private boolean faseFinaleSpesa = false;
	
	public boolean isFaseFinaleSpesa() {
	    return faseFinaleSpesa;
	}

	public void setFaseFinaleSpesa(boolean faseFinaleSpesa) {
	    this.faseFinaleSpesa = faseFinaleSpesa;
	}

	public List<String> getListaUtenteIniziale() {
	    return listaUtenteIniziale;
	}

	public void setListaUtenteIniziale(List<Prodotto> listaUtente) {
	    this.listaUtenteIniziale = Utils.copiaLista(listaUtente); //una nuova per evitare aliasing
	}

	void azzeraListaSupermarket() {
		listaSMdaVisitare.clear();
	}
	
	void aggiungiSupermarket(String s) {
		listaSMdaVisitare.add(s);
	}
	
	List<String> getListaSMdaVisitare() {
		return listaSMdaVisitare;
	}

	Location getPosizione() {
		return posizione;
	}

	void setPosizione(Location posizione) {
		this.posizione = posizione;
	}

	int getRaggioMassimo() {
		return raggioMassimo;
	}

	void setRaggioMassimo(int raggioMassimo) {
		this.raggioMassimo = raggioMassimo;
	}

}
