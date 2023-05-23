/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sqli.setup;

import static com.sqli.constants.OpenAIAutoDescriptionGeneratorConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.sqli.constants.OpenAIAutoDescriptionGeneratorConstants;
import com.sqli.service.OpenAIAutoDescriptionGeneratorService;


@SystemSetup(extension = OpenAIAutoDescriptionGeneratorConstants.EXTENSIONNAME)
public class OpenAIAutoDescriptionGeneratorSystemSetup
{
	private final OpenAIAutoDescriptionGeneratorService OpenAIAutoDescriptionGeneratorService;

	public OpenAIAutoDescriptionGeneratorSystemSetup(final OpenAIAutoDescriptionGeneratorService OpenAIAutoDescriptionGeneratorService)
	{
		this.OpenAIAutoDescriptionGeneratorService = OpenAIAutoDescriptionGeneratorService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		OpenAIAutoDescriptionGeneratorService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return OpenAIAutoDescriptionGeneratorSystemSetup.class.getResourceAsStream("/OpenAIAutoDescriptionGenerator/sap-hybris-platform.png");
	}
}
