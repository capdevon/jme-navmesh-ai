    private void initWorldMouseListener() {
        MouseEventControl.addListenersToSpatial(worldMap, new DefaultMouseListener() {
            @Override
            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                super.click(event, target, capture);

                // First clear existing pathGeometries from the old path finding:
                pathViewer.clearPath();

                if (getCharacters().size() == 1) {
                    DefaultQueryFilter filter = new BetterDefaultQueryFilter();

                    int includeFlags = POLYFLAGS_WALK | POLYFLAGS_DOOR | POLYFLAGS_SWIM | POLYFLAGS_JUMP;
                    filter.setIncludeFlags(includeFlags);

                    int excludeFlags = POLYFLAGS_DISABLED;
                    filter.setExcludeFlags(excludeFlags);

                    Node character = getCharacters().get(0);
                    Vector3f locOnMap = getLocationOnMap();
                    System.out.println("Compute path from " + character.getWorldTranslation() + " to " + locOnMap);

                    float[] m_spos = character.getWorldTranslation().toArray(null);
                    float[] m_epos = DetourUtils.toFloatArray(locOnMap);

                    //Extents can be anything you determine is appropriate.
                    float[] extents = new float[] {1,1,1};

                    Result<FindNearestPolyResult> startPoly = navQuery.findNearestPoly(m_spos, extents, filter);
                    Result<FindNearestPolyResult> endPoly = navQuery.findNearestPoly(m_epos, extents, filter);

                    if (startPoly.result.getNearestRef() != 0 && endPoly.result.getNearestRef() != 0) {

                        float yOffset = .5f;
                        pathViewer.putBox(ColorRGBA.Green, character.getWorldTranslation().add(0, yOffset, 0));
                        pathViewer.putBox(ColorRGBA.Yellow, locOnMap.add(0, yOffset, 0));

                        if (event.getButtonIndex() == MouseInput.BUTTON_LEFT) {
                            findPathStraight(character, filter, startPoly.result, endPoly.result);
                        }
                    } else {
                        System.err.println("Unable to find path");
                    }
                }
            }
        });
    }
