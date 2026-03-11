const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Moyenne = sequelize.define('moyenne', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    valeur:{
        type: DataTypes.DECIMAL(4,2),
        allowNull: false
    },
    valide:{
        type: DataTypes.BOOLEAN,
        defaultValue: false
    }
}, {
    timestamps:  true, //created_at
    underscored: true,
    updatedAt:   false, //on n'inclut pas updated_at
    indexes: [
        {
            unique: true,
            fields: ['eleve_id', 'semestre_id']
        }
    ]
})

module.exports = Moyenne