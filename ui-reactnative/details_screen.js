import { StatusBar } from 'expo-status-bar';
import * as React from 'react';
import { ScrollView, StyleSheet, Text, View, Button } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

const DetailsScreen= ({ navigation , route}) => {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#eff' }}>
      <Text>Details Screen</Text>
      <Text>This is {route.params.name} profile</Text>
    </View>
  );
}

export default DetailsScreen;