/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.static_data.dto;

import java.util.List;

/**
 *
 * @author bowen
 */
public class LanguageData {
    private final List<String> list;
        
    public LanguageData(List<String> list) {
        this.list = list;
    }
    
    public List<String> getLanguages() {
        return list;
    }

    @Override
    public String toString() {
        if (list.size() < 1) {
            return null;
        }
        return list.get(0);
    }
        
}
