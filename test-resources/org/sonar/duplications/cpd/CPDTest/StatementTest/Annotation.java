@Override
	private String createId() throws IOException {
		if (getParent() == null) 
			return localName;

		String id = getParent().getId();
		if (id.length() > 0) {
			id += File.separator;
		}
		id += localName;
		return id;
	}