/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2010 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.demo1.services;


import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.geolatte.common.transformer.TransformerSource;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * No comment provided yet for this class.
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 * @since SDK1.5
 */
public class RiverFeatureServerReader extends TransformerSource<RiverTo> {

    static public List<RiverTo> getRivers() {


        ClientRequest request = new ClientRequest("http://127.0.0.1:8080/featureserver/rest/tables/rivers?" + URLEncodedUtils.format(Arrays.asList(new BasicNameValuePair("cql", "int_name ilike 'Scheldt' OR int_name ilike 'Meuse'")), "utf-8"));
        request.accept(MediaType.APPLICATION_JSON);

        try {
            ClientResponse response;
            response = request.get();
            if (response.getStatus() == 200) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    RiverPaginationContainerTo res = mapper.readValue((String) response.getEntity(String.class), RiverPaginationContainerTo.class);

                    return res.getItems();
                } catch (Exception e) {
                    return Collections.emptyList();
                }
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Override
    protected Iterable<RiverTo> output() {

        return getRivers();
    }
}