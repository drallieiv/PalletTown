package com.pallettown.core;

import java.io.IOException;

import org.junit.Test;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class postmanTest {

	@Test
	public void test() throws IOException {
		OkHttpClient client = new OkHttpClient();

		RequestBody body = RequestBody.create(MediaType.parse("plain/txt"), "");

		Request request = new Request.Builder()
				.url("https://club.pokemon.com/us/pokemon-trainer-club/parents/sign-up")
				.post(body)
				.addHeader("origin", "https://club.pokemon.com")
				.addHeader("upgrade-insecure-requests", "1")
				.addHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
				.addHeader("content-type", "application/x-www-form-urlencoded")
				.addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.addHeader("referer", "https://club.pokemon.com/us/pokemon-trainer-club/parents/sign-up")
				.addHeader("accept-encoding", "gzip, deflate, br")
				.addHeader("accept-language", "fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4")
				.addHeader("cookie",
						"s_vnum=1518571065595%26vn%3D2; csrftoken=Miqyt5WHs5p3Pt9OQEZ8Pj5TtlUI9ErP; _sdsat_link=; _sdsat_businessUnit=pcom; _sdsat_Internal/External=internal; _sdsat_Language=en; s_fid=2F48A42F62899E04-10286BCBE9822D19; s_nr=1487101102845-Repeat; dob=1980-03-19; ptcs_session_id=x5e6i8jhytj0snsboqgzorq0xr1paqoc; django_language=en; zwaskh4dayfky8jw71lpotw2nr5ieh=1265237613; s_vnum=1518571065595%26vn%3D2; csrftoken=Miqyt5WHs5p3Pt9OQEZ8Pj5TtlUI9ErP; _sdsat_link=; _sdsat_businessUnit=pcom; _sdsat_Internal/External=internal; _sdsat_Language=en; s_fid=2F48A42F62899E04-10286BCBE9822D19; s_nr=1487101102845-Repeat; dob=1980-03-19; ptcs_session_id=x5e6i8jhytj0snsboqgzorq0xr1paqoc; django_language=en")
				.addHeader("cache-control", "no-cache")
				.build();

		Response response = client.newCall(request).execute();
		
		System.out.println(response.body().string());

		/*

		File debugFile = new File("ok.html");
		if(debugFile.exists()){
			debugFile.delete();
		}
	    BufferedSink sink = Okio.buffer(Okio.sink(debugFile));
	    sink.writeAll(response.body().source());
	    sink.close();
	    
	    */
	   
		
		
	}

}
