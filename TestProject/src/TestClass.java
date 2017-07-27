import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Lists;

import de.hybris.platform.core.Registry;
import de.hybris.platform.servicelayer.config.ConfigurationService;

public class TestClass {

	public static void main(String[] args) {
		
		ConfigurationService configurationService = Registry.getApplicationContext().getBean(ConfigurationService.class,"configurationService");
		
		Configuration configuration = configurationService.getConfiguration();
		
		Iterator<String> keys = configuration.getKeys();
		
		ArrayList<String> newArrayList = Lists.newArrayList(keys);
		
		Collections.sort(newArrayList);
		
		keys = newArrayList.iterator();
		
		File file = new File("C:\\apps\\configs.properties");
		
		FileWriter fw ;
		
		try {
		
		fw = new FileWriter(file);
		
		while(keys.hasNext())
			
		{
			String key = keys.next();
			

			
			String value = configuration.getString(key);
			
			StringBuilder sb = new StringBuilder();
			sb.append(key).append(" = ").append(value).append("\n");
			
				try {
					fw.write(sb.toString());
				} catch (IOException e) {

					e.printStackTrace();
				}
			
		
		}

		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fw != null) {
					
					fw.close();
				}
				
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

	}

}
