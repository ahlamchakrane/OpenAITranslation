/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at 30 mai 2023 à 11:09:56                      ---
 * ----------------------------------------------------------------
 */
package com.sqli.jalo;

import com.sqli.constants.OpenAIAutoDescriptionGeneratorConstants;
import com.sqli.jalo.CommentsModerationCronJob;
import com.sqli.jalo.ProductDescriptionGenerationCronJob;
import com.sqli.jalo.ProductDescriptionTranslationCronJob;
import de.hybris.platform.directpersistence.annotation.SLDSafe;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.extension.Extension;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type <code>OpenAIAutoDescriptionGeneratorManager</code>.
 */
@SuppressWarnings({"unused","cast"})
@SLDSafe
public class OpenAIAutoDescriptionGeneratorManager extends Extension
{
	protected static final Map<String, Map<String, AttributeMode>> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, Map<String, AttributeMode>> ttmp = new HashMap();
		DEFAULT_INITIAL_ATTRIBUTES = ttmp;
	}
	@Override
	public Map<String, AttributeMode> getDefaultAttributeModes(final Class<? extends Item> itemClass)
	{
		Map<String, AttributeMode> ret = new HashMap<>();
		final Map<String, AttributeMode> attr = DEFAULT_INITIAL_ATTRIBUTES.get(itemClass.getName());
		if (attr != null)
		{
			ret.putAll(attr);
		}
		return ret;
	}
	
	public CommentsModerationCronJob createCommentsModerationCronJob(final SessionContext ctx, final Map attributeValues)
	{
		try
		{
			ComposedType type = getTenant().getJaloConnection().getTypeManager().getComposedType("CommentsModerationCronJob");
			return (CommentsModerationCronJob)type.newInstance( ctx, attributeValues );
		}
		catch( JaloGenericCreationException e)
		{
			final Throwable cause = e.getCause();
			throw (cause instanceof RuntimeException ?
			(RuntimeException)cause
			:
			new JaloSystemException( cause, cause.getMessage(), e.getErrorCode() ) );
		}
		catch( JaloBusinessException e )
		{
			throw new JaloSystemException( e ,"error creating CommentsModerationCronJob : "+e.getMessage(), 0 );
		}
	}
	
	public CommentsModerationCronJob createCommentsModerationCronJob(final Map attributeValues)
	{
		return createCommentsModerationCronJob( getSession().getSessionContext(), attributeValues );
	}
	
	public ProductDescriptionGenerationCronJob createProductDescriptionGenerationCronJob(final SessionContext ctx, final Map attributeValues)
	{
		try
		{
			ComposedType type = getTenant().getJaloConnection().getTypeManager().getComposedType("ProductDescriptionGenerationCronJob");
			return (ProductDescriptionGenerationCronJob)type.newInstance( ctx, attributeValues );
		}
		catch( JaloGenericCreationException e)
		{
			final Throwable cause = e.getCause();
			throw (cause instanceof RuntimeException ?
			(RuntimeException)cause
			:
			new JaloSystemException( cause, cause.getMessage(), e.getErrorCode() ) );
		}
		catch( JaloBusinessException e )
		{
			throw new JaloSystemException( e ,"error creating ProductDescriptionGenerationCronJob : "+e.getMessage(), 0 );
		}
	}
	
	public ProductDescriptionGenerationCronJob createProductDescriptionGenerationCronJob(final Map attributeValues)
	{
		return createProductDescriptionGenerationCronJob( getSession().getSessionContext(), attributeValues );
	}
	
	public ProductDescriptionTranslationCronJob createProductDescriptionTranslationCronJob(final SessionContext ctx, final Map attributeValues)
	{
		try
		{
			ComposedType type = getTenant().getJaloConnection().getTypeManager().getComposedType("ProductDescriptionTranslationCronJob");
			return (ProductDescriptionTranslationCronJob)type.newInstance( ctx, attributeValues );
		}
		catch( JaloGenericCreationException e)
		{
			final Throwable cause = e.getCause();
			throw (cause instanceof RuntimeException ?
			(RuntimeException)cause
			:
			new JaloSystemException( cause, cause.getMessage(), e.getErrorCode() ) );
		}
		catch( JaloBusinessException e )
		{
			throw new JaloSystemException( e ,"error creating ProductDescriptionTranslationCronJob : "+e.getMessage(), 0 );
		}
	}
	
	public ProductDescriptionTranslationCronJob createProductDescriptionTranslationCronJob(final Map attributeValues)
	{
		return createProductDescriptionTranslationCronJob( getSession().getSessionContext(), attributeValues );
	}
	
	public static final OpenAIAutoDescriptionGeneratorManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (OpenAIAutoDescriptionGeneratorManager) em.getExtension(OpenAIAutoDescriptionGeneratorConstants.EXTENSIONNAME);
	}
	
	@Override
	public String getName()
	{
		return OpenAIAutoDescriptionGeneratorConstants.EXTENSIONNAME;
	}
	
}
