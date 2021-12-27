import { createTheme, PaletteOptions, Theme } from "@mui/material/styles";
import { } from "@mui/styles";


declare module '@mui/styles/defaultTheme' {
  interface DefaultTheme extends Theme { }
}

const palette = {
  mode: 'light',

  primary: {
    main: '#673AB7', // purple
    light: '#D1C4E9', // light purple
    dark: '#512DA8', // dark purple
    contrastText: '#212121', // black
  },
  secondary: {
    main: '#8BC34A', // bright green
    light: '',
    dark: '', 
    contrastText: '#757575' // medium gray
  },
  background: {
    default: 'rgb(249, 250, 252)', // primary bg colour for behind content boxes, light gray
    paper: 'rgb(255, 255, 255) ', // primary content bg colour, white
  },
  text: {
    primary: 'rgba(0,0,0,0.86)',
    secondary: 'rgb(209, 213, 219)', // inactive item, gray-ish white
    disabled: 'rgb(209, 213, 219)', // inactive item 
    hint: 'rgba(0,0,0,0.37)',
  },
  error: {
    main: '#f44336',
    light: '#f6685e',
    dark: '#aa2e25',
    contrastText: '#ffffff',
  },
  warning: {
    main: '#ff9800',
    light: '#ffac33',
    dark: '#b26a00',
    contrastText: 'rgba(0,0,0,0.87)',
  },
  info: {
    main: '#554971',
    light: '#796AA0',
    dark: '#413857',
    contrastText: '#ffffff',
  },
  success: {
    main: '#4caf50',
    light: '#6fbf73',
    dark: '#357a38',
    contrastText: 'rgba(0,0,0,0.87)',
  }
}

const siteTheme = createTheme({
  palette: palette as PaletteOptions,

});

export { siteTheme };
