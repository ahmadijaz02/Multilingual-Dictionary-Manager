package dal;

public class SQLFactory extends AbstartFactory {
	@Override
	public IWordDAO createDao() {
		return new DataAccessLayer();
	}

}
