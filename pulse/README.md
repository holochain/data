# Pulse

For now all steps are done by hand.

## Importer

The widget already includes sample data, so no need to run the importer to try it out.
Enter the importer folder. Check out the widget section below.

```
cd importer
```

### 1. Bootstrap

```
npm run bootstrap
```

### 2. Configure

- Adapt `.env` (you will need a [github access token](https://help.github.com/articles/creating-an-access-token-for-command-line-use/))
- Adapt `config.js`.

### 3. Import

```
node importer
```

Will generate `data.json`

### 4. Compile

```
node compile
```

Will generate `compiled.json`

### 5. Move to widget

```
mv compiled.json ../widget/src
```

## Widget

```
cd widget
```

### 1. Bootstrap

```
npm i
```

### 2. Dev

```
npm start
```

Open `localhost:3000`
