import * as React from 'react';

import getMuiTheme from 'material-ui/styles/getMuiTheme';
import {MuiThemeProvider, lightBaseTheme} from "material-ui/styles";
import Master from './components/Master';


const lightMuiTheme = getMuiTheme(lightBaseTheme);

import './App.css';

export interface IAppProp{

}

export interface IAppState{
  open : boolean;
}



class App extends React.Component<IAppProp, IAppState> {
  props : IAppProp;
  state : IAppState ={open : false};
  constructor(){
    super();
    this.setState ( {open: false});
    this.handleTouchTap = this.handleTouchTap.bind(this);
  }

handleTouchTap() {
   this.setState({open: !this.state.open});
}

  render() {  
    return (
       <MuiThemeProvider muiTheme={lightMuiTheme}>
          
        <Master  />          
         
      </MuiThemeProvider>
    );
  }
}

export default App;
