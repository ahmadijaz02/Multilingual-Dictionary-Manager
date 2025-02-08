package pl;

import dal.AbstartFactory;
import dal.IWordDAO;

public class Main {

	public static void main(String[] args) {
		IWordDAO DAO = AbstartFactory.getInstance().createDao();		
		PresentationLayer pl=new PresentationLayer(DAO);
		pl.setVisible(true);
		
	    }
	}