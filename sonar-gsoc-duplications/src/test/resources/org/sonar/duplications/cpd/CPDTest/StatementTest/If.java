if (getParent() == null) 
	return localName;

if (id.length() > 0) {
	id += File.separator;
} 
else if(id.length() == -1){
	id+= "../";
}

if (id.length() == 0)
{
	return localname;
} 

if (id.length() == 0) {	return localname; }