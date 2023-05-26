/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at 25 mai 2023 à 18:27:09                      ---
 * ----------------------------------------------------------------
 */
package com.sqli.jalo;

import de.hybris.platform.cronjob.jalo.CronJob;
import de.hybris.platform.directpersistence.annotation.SLDSafe;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.SessionContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type ProductDescriptionTranslationCronJob.
 */
@SLDSafe
@SuppressWarnings({"unused","cast"})
public class ProductDescriptionTranslationCronJob extends CronJob
{
	/** Qualifier of the <code>ProductDescriptionTranslationCronJob.Product_ID</code> attribute **/
	public static final String PRODUCT_ID = "Product_ID";
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>(CronJob.DEFAULT_INITIAL_ATTRIBUTES);
		tmp.put(PRODUCT_ID, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ProductDescriptionTranslationCronJob.Product_ID</code> attribute.
	 * @return the Product_ID - You can choose what product you want to generate its translations
	 */
	public String getProduct_ID(final SessionContext ctx)
	{
		return (String)getProperty( ctx, "Product_ID".intern());
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ProductDescriptionTranslationCronJob.Product_ID</code> attribute.
	 * @return the Product_ID - You can choose what product you want to generate its translations
	 */
	public String getProduct_ID()
	{
		return getProduct_ID( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ProductDescriptionTranslationCronJob.Product_ID</code> attribute. 
	 * @param value the Product_ID - You can choose what product you want to generate its translations
	 */
	public void setProduct_ID(final SessionContext ctx, final String value)
	{
		setProperty(ctx, "Product_ID".intern(),value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ProductDescriptionTranslationCronJob.Product_ID</code> attribute. 
	 * @param value the Product_ID - You can choose what product you want to generate its translations
	 */
	public void setProduct_ID(final String value)
	{
		setProduct_ID( getSession().getSessionContext(), value );
	}
	
}
