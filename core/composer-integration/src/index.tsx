import React from 'react';
import ReactDOM from 'react-dom';

import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'

import { StencilComposer, StencilClient, messages, siteTheme } from '@the-stencil-io/composer';

const locale = "en";

declare global {
  interface Window {
    portalconfig?: { server: { url: string, locked?: boolean }  },
  }
}

const { portalconfig } = window;

ReactDOM.render(
  <IntlProvider locale={locale} messages={messages[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <StencilComposer service={StencilClient.service({ url: portalconfig?.server.url ? portalconfig.server.url : "" })} locked={portalconfig?.server.locked}/>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>
  ,
  document.getElementById('root')
);