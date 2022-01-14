import { StatusBar } from 'expo-status-bar';
import * as React from 'react';
import { StyleSheet, Text, View, Button } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import  HomeScreen  from './home_screen';
import  DetailsScreen from './details_screen';
import  ListScreen from './list_screen';
import styles from './styles'

const Stack = createNativeStackNavigator();

export default function App() {
  return (
   <NavigationContainer>
        <Stack.Navigator>
            <Stack.Screen name="Home" component={HomeScreen} options={{title:'Welcome Page'}}/>
            <Stack.Screen name="Details" component={DetailsScreen} options={{title:'Details Page'}}/>
            <Stack.Screen name="Profile" component={DetailsScreen} options={{title:'Profile Page'}}/>
            <Stack.Screen name="Projects" component={ListScreen} options={{title:'Project List Page'}}/>
            <Stack.Screen name="Work" component={DetailsScreen} options={{title:'Work Details Page'}}/>
         </Stack.Navigator>
    </NavigationContainer>
  );
}
