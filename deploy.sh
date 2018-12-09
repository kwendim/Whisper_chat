#!/bin/bash
echo change directory to backend/functions
cd backend/functions
echo installing npm modules
npm install
cd ..
echo running firebase
firebase deploy --only functions --token 1/1uJ73PPWJO2LBkXsqOppcNvXpWwI3ahlo4AgX2OxVIk