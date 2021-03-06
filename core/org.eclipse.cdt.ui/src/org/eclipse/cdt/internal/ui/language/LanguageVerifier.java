/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;

/**
 * Analyzes and repairs language mapping configurations.
 */
public class LanguageVerifier {
	
	public static Map<String, ILanguage> computeAvailableLanguages() {
		ILanguage[] registeredLanguages = LanguageManager.getInstance().getRegisteredLanguages();
		Map<String, ILanguage> languages = new TreeMap<String, ILanguage>();
		for (int i = 0; i < registeredLanguages.length; i++) {
			languages.put(registeredLanguages[i].getId(), registeredLanguages[i]);
		}
		return languages;
	}
	
	public static String computeAffectedLanguages(Set<String> missingLanguages) {
		Iterator<String> languages = missingLanguages.iterator();
		StringBuffer buffer = new StringBuffer();
		while (languages.hasNext()) {
			buffer.append('\n');
			buffer.append(languages.next());
		}
		return buffer.toString();
	}

	public static Set<String> removeMissingLanguages(ProjectLanguageConfiguration config, ICProjectDescription description, Map<String, ILanguage> availableLanguages) {
		Set<String> missingLanguages = new TreeSet<String>();
		
		// Check file mappings
		Iterator<Entry<String, Map<String, String>>> fileConfigurationMappings = config.getFileMappings().entrySet().iterator();
		while (fileConfigurationMappings.hasNext()) {
			Entry<String, Map<String, String>> entry = fileConfigurationMappings.next();
			String path = entry.getKey();
			Map<String, String> configurationLanguageMappings = entry.getValue();
			Iterator<Entry<String, String>> mappings = configurationLanguageMappings.entrySet().iterator();
			while (mappings.hasNext()) {
				Entry<String, String> mapping = mappings.next();
				String configurationId = mapping.getKey();
				String languageId = mapping.getValue();
				if (!availableLanguages.containsKey(languageId)) {
					missingLanguages.add(languageId);
					ICConfigurationDescription configuration = description.getConfigurationById(configurationId);
					config.removeFileMapping(configuration, path);
				}
			}
		}
		
		// Check content type mappings
		Iterator<Entry<String, Map<String, String>>> configurationContentTypeMappings = config.getContentTypeMappings().entrySet().iterator();
		while (configurationContentTypeMappings.hasNext()) {
			Entry<String, Map<String, String>> entry = configurationContentTypeMappings.next();
			String configurationId = entry.getKey();
			Map<String, String> contentTypeLanguageMappings = entry.getValue();
			Iterator<Entry<String, String>> mappings = contentTypeLanguageMappings.entrySet().iterator();
			while (mappings.hasNext()) {
				Entry<String, String> mapping = mappings.next();
				String contentTypeId = mapping.getKey();
				String languageId = mapping.getValue();
				if (!availableLanguages.containsKey(languageId)) {
					missingLanguages.add(languageId);
					ICConfigurationDescription configuration = description.getConfigurationById(configurationId);
					config.removeContentTypeMapping(configuration, contentTypeId);
				}
			}
		}
		
		return missingLanguages;
	}

	public static Set<String> removeMissingLanguages(WorkspaceLanguageConfiguration config, Map<String, ILanguage> availableLanguages) {
		Set<String> missingLanguages = new TreeSet<String>();
		
		// Check content type mappings
		Iterator<Entry<String, String>> contentTypeMappings = config.getWorkspaceMappings().entrySet().iterator();
		while (contentTypeMappings.hasNext()) {
			Entry<String, String> entry = contentTypeMappings.next();
			String contentTypeId = entry.getKey();
			String languageId = entry.getValue();
			if (!availableLanguages.containsKey(languageId)) {
				missingLanguages.add(languageId);
				config.removeWorkspaceMapping(contentTypeId);
			}
		}
		
		return missingLanguages;
	}
}
