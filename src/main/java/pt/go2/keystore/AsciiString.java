
package pt.go2.keystore;

/**
 * Immutable ascii string
 * 
 * More memory efficient than String by using byte instead of char.
 * 
 * Other future optimizations are possible.
 * 
 * 
 * MUST OVERRIDE BOTH hashCode() and equals(Object). hashCode() value must be calculated in c'tor for faster lookups in Map
 * 
 * @author vilaca
 *
 */
class AsciiString {

	private final byte[] inner;
	private final int hashcode;
	
	AsciiString(String str)
	{
		inner = str.getBytes();
		hashcode = str.hashCode();
	}
	
	@Override
	public int hashCode()
	{
		return hashcode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj.getClass() != getClass())
			return false;

		byte[] inner = ((AsciiString) obj).inner;
		
		if (this.inner.length != inner.length)
			return false;
		
		for ( int i = 0; i < this.inner.length; i++)
		{
			byte b = this.inner[i];
			byte b2 = inner[i];
			
			if ( b != b2 ) return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return new String(inner);
	}

}
