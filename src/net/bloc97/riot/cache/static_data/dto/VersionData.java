/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.static_data.dto;

import java.util.List;
import net.rithms.riot.api.Dto;

/**
 *
 * @author bowen
 */
public class VersionData extends Dto {
    private final List<String> list;
        
    public VersionData(List<String> list) {
        this.list = list;
    }
    
    public List<String> getVersions() {
        return list;
    }

    public String getLatestVersion() {
        if (list.size() < 1) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public String toString() {
            return getLatestVersion();
    }
        
        
}
