package com.manoelneto.taxi99.rest;

import com.manoelneto.taxi99.model.Driver;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Rest(rootUrl = "https://api.99taxis.com", converters = {MappingJackson2HttpMessageConverter.class})
public interface IClientAppRest {

    @Get("/lastLocations?sw={sw}&ne={ne}")
    Driver[] getTaxistasDisponiveis(String sw, String ne);

}
