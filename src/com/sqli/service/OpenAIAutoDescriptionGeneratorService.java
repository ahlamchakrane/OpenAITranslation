/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sqli.service;

public interface OpenAIAutoDescriptionGeneratorService
{
	String getHybrisLogoUrl(String logoCode);

	void createLogo(String logoCode);
}
