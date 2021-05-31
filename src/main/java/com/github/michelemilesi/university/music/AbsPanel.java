package com.github.michelemilesi.university.music;

import javax.swing.JPanel;
import java.awt.*;

public abstract class AbsPanel extends JPanel {
	private String description;
	private String filter_name;
	private boolean attivo;
	private boolean visible;
	protected Filter filtro;
	
	protected abstract void init();
	
	public abstract void loadFilter(String descrizione, String nome_filtro) throws Exception;
	
	public abstract void setParameter(String[] elenco) throws Exception;

	protected void setFilter(Filter filtro) {
		this.filtro = filtro;
	}

	protected void setFilterName(String name) {
		this.filter_name = name;
	}
	
	protected void setDescription(String des) {
		this.description = des;
	}
	
	public String getFilterName() {
		return filter_name;
	}
	
	public Filter getFilter() {
		return filtro;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Sound applica(Sound audio) {
		init();
		audio.reset();
		return filtro.process(audio);
	}

	public void attiva(boolean attivo) {
		this.attivo = attivo;
	}
	
	public void visualizza(boolean visible) {
		this.visible = visible;
		setVisible(visible);
	}

	public boolean isSelected() {
		return attivo;
	}

	public boolean isVisible() {
		return visible;
	}
	
}