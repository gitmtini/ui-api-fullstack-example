import { StatusBar } from 'expo-status-bar';
import * as React from 'react';
import { FlatList,ScrollView, StyleSheet, Text, View, Button } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import styles from './styles'

const ListScreen= ({ navigation , route}) => {
  return (
    <View style={[styles.listContainer]}>
      <Text>List Screen</Text>
      <Text>This is {route.params.name} list</Text>
      <FlatList data={[
            {key: 'Item1'},
            {key: 'Item2'},
            {key: 'Item3'},
            {key: 'Item4'},
            {key: 'Item5'},
            {key: 'Item6'},
            {key: 'Item7'},
            {key: 'Item8'}
      ]}
      renderItem={({item}) => <Text onPress={() => navigation.navigate('Work', { name: item.key })} >{item.key}</Text> }
      />
    </View>
  );
}

export default ListScreen;