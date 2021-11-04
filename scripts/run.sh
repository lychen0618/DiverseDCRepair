# shellcheck disable=SC1113
#/usr/bin/env bash
DATASET=$1
DATASET_PATH='/home/lychen/labOfPaper/EnumDCRepair/data/input/dataset'
SCRIPTS_PATH='/home/lychen/labOfPaper/EnumDCRepair/scripts'
CONFIG_EGTASK_PATH='/home/lychen/labOfPaper/EnumDCRepair/config/egtask/'
echo "DATASET = $DATASET"
echo "DATASET_PATH = $DATASET_PATH"
echo "SCRIPTS_PATH = $SCRIPTS_PATH"
echo "CONFIG_EGTASK_PATH = $CONFIG_EGTASK_PATH"
conda init
source ~/.bashrc
conda activate paperlab
conda info --envs
if [ $# == 1 ]; then
  cd $SCRIPTS_PATH || exit
  python init.py --dataset "${DATASET}"
fi

if [ $# == 2 ]; then
  cd $DATASET_PATH || exit
  DATA_PATH=$(pwd)'/'$DATASET
  echo "DATA_PATH = $DATA_PATH"
  file_or_dir=$(ls "$DATA_PATH")
  for file in $file_or_dir; do
    if [ -d "$DATA_PATH""/""$file" ]; then
      for ra in high; do
        cd $SCRIPTS_PATH || exit
        python run_egtask.py --dataset "${DATASET}" \
          --tupleNum "${file}" \
          --errorRate "$2" \
          --repairAbility $ra
        echo "run_egtask finished!"
        cd "$CONFIG_EGTASK_PATH"'/'"$DATASET" || exit
        ~/labOfPaper/BART/Bart_Engine/run.sh "$DATASET"'_egtask_copy.xml'
        echo "bart $DATASET $file $2 $ra"
      done
    fi
  done
fi

if [ $# == 3 ]; then
  cd $DATASET_PATH || exit
  DATA_PATH=$(pwd)'/'$DATASET
  echo "DATA_PATH = $DATA_PATH"
  for ra in high; do
    cd $SCRIPTS_PATH || exit
    python run_egtask.py --dataset "${DATASET}" \
      --tupleNum "$2" \
      --errorRate "$3" \
      --repairAbility $ra
    echo "run_egtask finished!"
    cd "$CONFIG_EGTASK_PATH"'/'"$DATASET" || exit
    ~/labOfPaper/BART/Bart_Engine/run.sh "$DATASET"'_egtask_copy.xml'
    echo "bart $DATASET $2 $3 $ra"
  done
fi
