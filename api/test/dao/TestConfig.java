package dao;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.junit.Test;

import io.diy.api.props.Configurations;
import junit.framework.Assert;

public class TestConfig {
	
	@Test
	public void testReadSecretKey(){
		Configurations conf = Configurations.init();
		Assert.assertTrue(null!=conf.SIGNING_SECRET_KEY);
		System.out.println(conf.SIGNING_SECRET_KEY);
		
		Assert.assertTrue(null!=conf.JWT_HEADER_ATTR);
		System.out.println(conf.JWT_HEADER_ATTR);
	}
	
	@Test
	public void test(){
		BigDecimal amount = new BigDecimal(3360);
		//amount.setScale(2, BigDecimal.ROUND_DOWN);
		 DecimalFormat df = new DecimalFormat();
         df.setMaximumFractionDigits(2);
         df.setMinimumFractionDigits(2);
         df.setGroupingUsed(false);
         String strVal=df.format(amount);
 		System.out.println(strVal);

	}

}
