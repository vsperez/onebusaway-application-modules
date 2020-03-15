var dictionary=new Map();

function getValueFor(key)
{
	let returnValue=dictionary.get(key);
	if(returnValue==null || key==undefined)
		return "please add key for "+key;
	return returnValue;
	
}